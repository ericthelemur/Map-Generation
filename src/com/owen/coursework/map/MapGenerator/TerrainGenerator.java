package com.owen.coursework.map.MapGenerator;

import com.owen.coursework.Game;
import com.owen.coursework.Utilities;
import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.util.*;
import java.util.Queue;
import java.util.logging.Logger;

public class TerrainGenerator {
    private final static Logger logger = Logger.getLogger(Game.class.getName());

    private Dimension mapSize;

    private Noise perturbX, perturbY, elevation, rainfall, radPerturb;

    private final double perturbFactor = 0.2;
    private final double seaPercent = 0.5;
    private double seaLevel;

    private int landMassCount, waterbodyCount;
    private Hex nullHex = new Hex(-1, -1);

    public TerrainGenerator(int width, int height, long seed) {
        mapSize = new Dimension(width, height);
        logger.info("BOUNDS "+Map.bounds);

        NoiseBuilder noiseBuilder = new NoiseBuilder(Map.bounds);
        Random rand = new Random(seed);

        perturbX = noiseBuilder.setOctaves(4).build(Noise.NoiseTypes.NoTransform, rand.nextLong());
        perturbY = noiseBuilder.setOctaves(4).build(Noise.NoiseTypes.NoTransform, rand.nextLong());
        radPerturb = noiseBuilder.setOctaves(4).build(Noise.NoiseTypes.NoTransform, rand.nextLong());
        elevation = noiseBuilder.setScale(384).setOctaves(16).build(Noise.NoiseTypes.Ridged  , rand.nextLong());
        rainfall = noiseBuilder.build(Noise.NoiseTypes.Billowed, rand.nextLong());
    }

    public void generate() {
        Game.updateLoadingMessage("Generating Elevation Map");
        generateElevation();

        Game.updateLoadingMessage("Generating Rainfall Map");
        generateRainfall();

        Game.updateLoadingMessage("Finding Sea Level");
        findSeaLevel();

        Game.updateLoadingMessage("Running Rivers");
        runRivers();

        Game.updateLoadingMessage("Generating Biome Map");
        generateBiomes();
    }

    public void generateElevation() {
        logger.info(String.format("Generating %sx%s with perturbFactor %.2f", mapSize.width, mapSize.height, perturbFactor));

        // Initial Values (+perturbation)
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            MapPosition mp = new MapPosition(x, y);
            WorldPosition wp = mp.toWorldPosition();

            double px = perturbX.evaluate(wp.x, wp.y)*perturbFactor, py = perturbY.evaluate(wp.x, wp.y)*perturbFactor;

            Map.getHex(x, y).elevation = elevation.evaluate(px*Map.bounds.width+wp.x, py*Map.bounds.height+wp.y);
        }

        // Normalize 1, mult by distance from edge
//        double min = 1, max = 0;
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            MapPosition mp = new MapPosition(x, y);
            WorldPosition wp = mp.toWorldPosition();

//            double e = Utilities.normalize(Map.getHex(x, y).elevation, elevation.getMin(), elevation.getMax());
            double e = Map.getHex(x, y).elevation;
            // Avg. both methods, good compromise between natural shape, filling most of the grid and having no land touching the edge
            double manha = 2*Math.max(Math.abs(0.5 - (wp.x / Map.bounds.width)), Math.abs(0.5 - (wp.y / Map.bounds.height)));
            double pythag = Math.sqrt(Math.pow(0.5 - (wp.x / Map.bounds.width), 2) + Math.pow(0.5 - (wp.y / Map.bounds.height), 2));
            double d = 1-Math.pow(0.5*(manha+pythag), 1.2)+radPerturb.evaluate(wp.x, wp.y);
            e *= d;

//            if (e > max) max = e;
//            if (e < min) min = e;

            Map.getHex(x, y).elevation = e;
        }

        // Normalize again
//        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
//
//            double e = Utilities.normalize(Map.getHex(x, y).elevation, min, max);
//            Map.getHex(x, y).elevation = e;
//        }

    }

    public void generateRainfall() {        // Generate rainfall with a layer of perlin noise, then normalize it, and apply a gradient
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            MapPosition mp = new MapPosition(x, y);
            WorldPosition wp = mp.toWorldPosition();
            double r = rainfall.evaluate(wp.x, wp.y);
            Map.getHex(x, y).rainfall = r;
        }

        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            Map.getHex(x, y).rainfall = Utilities.normalize(Map.getHex(x, y).rainfall, rainfall.getMin(), rainfall.getMax(), 0.2, 1.5) * (1 - (double) y / mapSize.height);
        }
    }

    // like https://mewo2.com/notes/terrain/, but with priority-flood instead of Planchon-Darboux
    // Priority Flood Algorithm (unimproved version) https://arxiv.org/abs/1511.04463
    // Should fill up depressions as lakes, but too many, so not visually filling
    private void runRivers() {
        PriorityQueue<Hex> open = new PriorityQueue<>(1000, Comparator.comparingDouble(hex -> hex.elevation));

        // Fill with initial values (edges)
        for (int i = 0; i < Map.size.width; i++) {
            Hex hex1 = Map.getHex(i, 0);
            open.add(hex1);
            hex1.flow = nullHex;

            hex1 = Map.getHex(i, Map.size.height-1);
            open.add(hex1);
            hex1.flow = nullHex;
        }

        for (int i = 1; i < Map.size.height-1; i++) {
            Hex hex1 = Map.getHex(0, i);
            open.add(hex1);
            hex1.flow = nullHex;

            hex1 = Map.getHex(Map.size.width-1, i);
            open.add(hex1);
            hex1.flow = nullHex;
        }

        // Calculate drainage basins
        while (!open.isEmpty()) {
            Hex current = open.remove();        // Go through all hexes, ordered by lowest neighbouring hex
            for (Hex neighbour: current.getNeighbours()) {
                if (neighbour.flow == null) {   // If neighbour is unprocessed
                    if (current.getWaterElevation() > neighbour.getWaterElevation())    // Set water height if neighbour is lower
                        neighbour.waterElevation = current.waterElevation;

                    neighbour.flow = current;
                    current.sources.add(neighbour);
                    open.add(neighbour);
                }
            }
        }

        // Run rivers
        double maxSize = 0;
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            Hex hex = Map.getHex(x, y);
            double val = 0.5+hex.rainfall;

            if (hex.flow == nullHex) hex.flow = null;
            while (hex != null && hex != nullHex) {
                hex.waterFlux += val;
                if (hex.waterFlux > maxSize && hex.elevation > Map.seaLevel)
                    maxSize = hex.waterFlux;
                hex = hex.flow;
            }
        }
        Map.maxRiverSize = maxSize;
    }

    public void findSeaLevel() {        // Find sea level so a certain % of land is above
        double[] levels = new double[mapSize.width*mapSize.height];
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) levels[x*mapSize.height+y] = Map.getHex(x, y).elevation;
        Arrays.sort(levels);
        seaLevel = levels[(int) (levels.length*seaPercent)];
        Map.seaLevel = seaLevel;
        logger.info(String.format("Sea Level: %.3f", seaLevel));
    }

    public void generateBiomes() {
        // Basic assignings
        for (int x = 0; x < mapSize.width; x++)
            for (int y = 0; y < mapSize.height; y++) {
                Hex hex = Map.getHex(x, y);
                hex.biome = assignBiome(hex);
            }

        // Setting sea to surround land
        for (int x = 0; x < mapSize.width; x++)
            for (int y = 0; y < mapSize.height; y++) {
                Hex hex = Map.getHex(x, y);
                if (hex.biome == Map.Biome.Ocean)
                    for (Hex neighbour : hex.getRings(3)) {
                        // Set sea to surround coast
                        if (neighbour.biome != Map.Biome.Ocean && neighbour.biome != Map.Biome.Sea)
                            hex.biome = Map.Biome.Sea;
                    }
            }

        Game.updateLoadingMessage("Detecting Islands");
        // Landmasses and Waterbodies
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            Hex hex = Map.getHex(x, y);     // For each hex, if not assigned, flood fill outwards from hex with type
            if (hex.area == null) {
                if (hex.biome.getType() == Map.BiomeTag.Ocean) fillArea(x, y, Map.BiomeTag.Ocean);
                else fillArea(x, y, Map.BiomeTag.Land);
            }
        }
        logger.info(String.format("LM: %d WB: %d", Map.landmasses.size(), Map.waterbodies.size()));

    }

    private void fillArea(int sx, int sy, Map.BiomeTag biomeTag) {
        Map.Area area;
        switch (biomeTag) {     // Creates new area of correct type
            case Land:
                area = new Map.Landmass(++landMassCount);
                Map.landmasses.add((Map.Landmass) area);
                break;
            case Ocean:
                area = new Map.Waterbody(++waterbodyCount);
                Map.waterbodies.add((Map.Waterbody) area);
                break;
            default: return;
        }

        Queue<MapPosition> frontier = new LinkedList<MapPosition>() {{add(new MapPosition(sx, sy));}};
        while (!frontier.isEmpty()) {               // Like Breath-First search
            MapPosition current = frontier.poll();
            if (current == null) continue;
            Hex hex = Map.getHex(current);      // Get hex

            Map.getHex(current).area = area;    // Process (set area)
            area.addHex(hex);

            ArrayList<MapPosition> neighbours = hex.getNeighboursPos();     // Add neighbours which are unprocessed and
            for (MapPosition mapPosition : neighbours)                      // of correct type
                if (!frontier.contains(mapPosition) && Map.getHex(mapPosition).area == null &&
                        Map.getHex(mapPosition).biome.containsTag(biomeTag))
                    frontier.add(mapPosition);
        }
    }


    private Map.Biome assignBiome(Hex hex) {    // Approximates a Whittaker diagram, perhaps too much detail
        double e = hex.elevation, r = hex.rainfall;

        if (e < getSeaLevel()) return Map.Biome.Ocean;

        e = Utilities.normalize(e, getSeaLevel(), 1, 0, 1);
        if (e < 0.02) return Map.Biome.Beach;

        if (e > 0.95) return Map.Biome.Snow;

        if (e > 0.67) {
            if (r < 0.3) return Map.Biome.Scorched;
            if (r < 0.7) return Map.Biome.Bare;
            return Map.Biome.Tundra;
        }
        if (e > 0.55) {
            if (r < 0.33) return Map.Biome.TemperateDesert;
            if (r < 0.66) return Map.Biome.Shrubland;
            return Map.Biome.Tundra;
        }
        if (e > 0.22) {
            if (r < 0.15) return Map.Biome.TemperateDesert;
            if (r < 0.45) return Map.Biome.Grassland;
            if (r < 0.83) return Map.Biome.TemperateDeciduousForest;
            return Map.Biome.TemperateRainforest;
        }
        if (r < 0.15) return Map.Biome.SubtropicalDesert;
        if (r < 0.4) return Map.Biome.Grassland;
        if (r < 0.6) return Map.Biome.TropicalSeasonalForest;
        return Map.Biome.TropicalRainforest;
    }

    public double getSeaLevel() {
        return seaLevel;
    }
}


