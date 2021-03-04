package com.owen.coursework.map.MapGenerator;

import com.owen.coursework.Game;
import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Entities.MapEntities.Capital;
import com.owen.coursework.map.Entities.MapEntities.Town;
import com.owen.coursework.map.Entities.MapEntities.Nation;
import com.owen.coursework.map.Entities.MapEntities.Tree;
import com.owen.coursework.map.NameGenerator.BackOffGenerator;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;

public class MapGenerator {
    private Dimension mapSize;
    private Random rand;
    private TerrainGenerator terrainGenerator;
    private BackOffGenerator welshGenerator, germanGenerator;
    private final int CAPITALNUMB = 6, CAPNUMBVAR = 2;


    public MapGenerator(Dimension mapSize, long seed) {
        this.mapSize = mapSize;

        rand = new Random(seed);
        terrainGenerator = new TerrainGenerator(mapSize.width, mapSize.height, rand.nextLong());
        welshGenerator = new BackOffGenerator(4, "welsh.txt", rand.nextLong());
        germanGenerator = new BackOffGenerator(4, "german.txt", rand.nextLong());
    }

    public void generate() {
        Game.updateLoadingMessage("Generating terrain");
        terrainGenerator.generate();

        Game.updateLoadingMessage("Placing Cities");
        placeCities();

        Game.updateLoadingMessage("Calculating territory");
        fillNations();

        Game.updateLoadingMessage("Routing Roads");
        routeRoads();

        Game.updateLoadingMessage("Placing Trees");
        placeTrees();
    }

    private PriorityQueue<Hex> generateCityProbabilities() {
        PriorityQueue<Hex> order = new PriorityQueue<>((int) (1.1*mapSize.width*mapSize.height*(1-terrainGenerator.getSeaLevel())),
                Comparator.comparingDouble((Hex hex) -> hex.cityChance).reversed());

        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            Hex hex = Map.getHex(x, y);
            if (hex.biome.getType() == Map.BiomeTag.Ocean) continue;

            double wfSum = hex.waterFlux;  // Sum waterFlux of hex and neighbours: cities are more likely to appear on rivers
            for (int i = 1; i < 2; i++)
                for (Hex hex1 : hex.getRing(i))
                    wfSum += hex1.biome.getType() == Map.BiomeTag.Land ? Math.sqrt(hex1.waterFlux) : 3;

            hex.cityChance = Math.sqrt(wfSum);
            order.add(hex);
        }
        return order;
    }

    private final int MAXSIZE = 10000;
    private void placeCities() {
        PriorityQueue<Hex> queue = generateCityProbabilities();

        int capNumb = CAPITALNUMB - CAPNUMBVAR + rand.nextInt(CAPNUMBVAR * 2), capCount = 0;
        while (capCount < capNumb && !queue.isEmpty()) {
            Hex hex = queue.remove();
            if (!isNearCity(hex, Town.TownType.CAPITAL)) {   // If far enough away from other capitals
                Nation nation = new Nation(hex, (int) (MAXSIZE * (0.8+rand.nextDouble()*0.4)), rand.nextDouble() < 0.5 ? welshGenerator : germanGenerator);
                                                            // Create nation around capital, with a random population and culture
                Map.addNation(nation);
                hex.town = nation.capital;
                capCount++;
            }
        }

        // Place remaining cities
        int count = 0;
        int limit = (int) (queue.size() * 0.2);   // Don't try the worst 20% of tiles, so coverage isn't completely uniform
        while (queue.size() > limit) {
            Hex hex = queue.remove();
            if (!isNearCity(hex, Town.TownType.TOWN)) {  // If no cities too near by
                Town c = new Town(hex.x, hex.y, "", count++);
                Map.cities.add(c);
                hex.town = c;

                // Remove surrounding square, so doesn't need to process so many (not perfect, just the closest ones)
                MapPosition tl = new MapPosition(Math.max(hex.x- Town.TownType.TOWN.influence/2, 0), Math.max(hex.y- Town.TownType.TOWN.influence/2, 0)),
                            br = new MapPosition(Math.min(hex.x+ Town.TownType.TOWN.influence/2, Map.size.width), Math.min(hex.y+ Town.TownType.TOWN.influence/2, Map.size.height));
                for (int i = tl.x; i < br.x; i++)
                    for (int j = tl.y; j < br.y; j++)
                        queue.remove(Map.getHex(i, j));
            }
        }
    }

    private boolean isNearCity(int x, int y, Town.TownType type) {
        for (Town c : Map.cities) if (c.getDistanceSq(x, y) < type.influence*type.influence) return true;
        return false;
    }

    private boolean isNearCity(MapPosition mp, Town.TownType type) {
        return isNearCity(mp.x, mp.y, type);
    }



    private void fillNations() {        // Fills the territory of each nation outwards
        PriorityQueue<Territory> queue = new PriorityQueue<>(100, Comparator.comparingDouble(Territory::getScore));

        for (Nation n : Map.nations)   // Initialize the queue
            for (Hex nei : Map.getHex(n.capital).getNeighbours())
                queue.add(new Territory(n, 0, nei, Map.getHex(n.capital.x, n.capital.y)));

        while (!queue.isEmpty()) {  // Edited breadth-first search
            Territory current = queue.remove();
            if (current.hex.nation != null) continue;

            current.hex.nation = current.nation;
            current.nation.addTerritory(current.hex);

            if (current.hex.town != null) {    // If a town, set nation and name
                Town town = current.hex.town;
                town.nation = current.nation;
                if (town.name.equals("")) town.setName(current.nation.generateName());
            }

            if (current.hex.x == 0 || current.hex.x == Map.size.width-1 || current.hex.y == 0 || current.hex.y == Map.size.height-1) {
                current.nation.addBorder(current.hex);
            }

            for (Hex neighbour : current.hex.getNeighbours()) { // Add neighbours to the queue if unprocessed
                if (neighbour.nation != null) {
                    if (current.nation != neighbour.nation) {
                        current.nation.addBorder(current.hex);
                        neighbour.nation.addBorder(neighbour);
                    }
                    continue;
                }
                queue.add(new Territory(current.nation, current.score, neighbour, current.hex));
            }
        }

        for (Nation n : Map.nations) {     // When nations are finished, set their colour to be a HSV colour, with a spread of hues
            n.colour = Color.getHSBColor((float) Map.nations.indexOf(n)/Map.nations.size(), 1, 1);

            // Set town size as Zipf's Law states, population is approximately inversely proportional to its size rank
            ArrayList<Town> towns = n.towns;
            towns.sort(Comparator.comparingInt((Town town) -> town.size).reversed());
            for (int i = 1; i < towns.size(); i++)
                towns.get(i).size = (int) (n.capital.size/(i+1)*(0.8+rand.nextDouble()*0.4));

            generateBorderLines(n);
        }
    }

    private void generateBorderLines(Nation n) {
        ArrayList<Hex> border = floodBorder(n.border.get(0), n);    // Floods border round from first member to get ordered border

        Path2D.Double borderShape = new Path2D.Double();    // Construct Shape object for border
        int l = border.size() - 1;
        for (int i = 0; i < border.size(); i++) {

            // Get positions for curve
            Hex first = border.get(i <= 0 ? l : (i - 1)), mid = border.get(i), last = border.get(i >= l ? 0 : (i + 1));
            WorldPosition swp = first.toWorldPosition(), wp = mid.toWorldPosition(), fwp = last.toWorldPosition();

            swp = new WorldPosition((swp.x+wp.x)/2, (swp.y+wp.y)/2);
            fwp = new WorldPosition((fwp.x+wp.x)/2, (fwp.y+wp.y)/2);

            borderShape.moveTo(swp.x, swp.y);
            borderShape.curveTo(swp.x, swp.y, wp.x, wp.y, fwp.x, fwp.y);
        }
        n.borderShape = borderShape;
    }

    private ArrayList<Hex> floodBorder(Hex start, Nation n) {   // Floods in one direction first, then tries opposite direction
        ArrayList<Hex> l = floodSection(start, n, null), r = floodSection(start, n, l);
        Collections.reverse(l);
        if (l.get(0).getNeighbours().contains(r.get(r.size()-1))) r.add(l.get(0));
        l.addAll(r);
        return l;
    }

    private ArrayList<Hex> floodSection(Hex start, Nation n, ArrayList<Hex> otherSide) {
        ArrayList<Hex> borderSection = new ArrayList<>();
        Hex current = start;
        int depth = mapSize.width*4;
        while (current != null && depth > 0) {  // While next hex exists
            borderSection.add(current);
            depth--;
            boolean changed = false;
            for (Hex hex : current.getNeighbours()) {   // First neighbour who: is not already added, is in the border,
                if (!borderSection.contains(hex) && n.border.contains(hex) &&   // not in other half (if it exists),
                        (otherSide == null || !otherSide.contains(hex)) &&      // and shares an enemy hex neighbour with
                        sharesBorder(hex, current)) {                           // the current hex
                    current = hex;
                    changed = true;
                    break;
                }
            }
            if (!changed) {                         // If none that match that, find neighbour who is already in the border
                for (Hex hex : current.getNeighbours()) {       // for 1 wide areas in the territory
                    if (n.border.contains(hex) &&
                            (otherSide == null || !otherSide.contains(hex)) &&
                            sharesBorder(hex, current)) {
                        current = hex;
                        changed = true;
                        break;
                    }
                }
            }
            if (current.equals(start) || !changed) break;   // If none found or back at start, end
        }
        return borderSection;
    }

    private boolean sharesBorder(Hex hex1, Hex hex2) {  // Only flood to hexes that share a border tile, so wrapping works correctly
        ArrayList<Hex> hex2Neighbours = hex2.getNeighbours();
        for (Hex hex : hex1.getNeighbours()) {
            if (hex.nation != hex1.nation && hex2Neighbours.contains(hex)) return true;
        }

        // Shares border if on same edge of map
        return (hex1.x == hex2.x && (hex1.x == 0 || hex1.x == Map.size.width - 1)) ||
                (hex1.y == hex2.y && (hex1.y == 0 || hex1.y == Map.size.height - 1));

    }

    private class Territory {       // Used for the multiple source breadth first search above
        public Nation nation;
        public Hex hex, source;
        public double score;

        public Territory(Nation nation, double oldScore, Hex hex, Hex source) {
            this.nation = nation;
            this.hex = hex;
            this.source = source;
            this.score = oldScore+getNewScore();
        }

        public double getNewScore() {       // Prioritizes flatter land, lower water flux (so borders are along rivers) and land tiles
            double height =  hex.elevation - source.elevation;
            double diff = 1 + 0.25 * Math.pow(height, 2) + Math.sqrt(hex.waterFlux)*10;
            if (hex.biome.getType() == Map.BiomeTag.Ocean) diff = 100;
            if (hex.biome.getType() != source.biome.getType()) return 1000;   // Especially if changing from land to sea
            return diff;
        }

        public double getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Territory territory = (Territory) o;
            return Objects.equals(nation, territory.nation) &&
                    Objects.equals(hex, territory.hex) &&
                    Objects.equals(source, territory.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nation, hex, source);
        }
    }



    private final int maxDistance = 35, maxDistanceSq = maxDistance*maxDistance;
    private void routeRoads() {
        PriorityQueue<Map.RoadRoute> roadConnections = new PriorityQueue<>(20, Comparator.comparingDouble(Map.RoadRoute::getScore).reversed());
        ArrayList<Town> cities = Map.cities;

        // Finds cities to connect (connect capitals and nearby towns), and ranks them
        for (int i = 0; i < cities.size(); i++)
            for (int j = i+1; j < cities.size(); j++) {
                Town c1 = cities.get(i), c2 = cities.get(j);
                if ((c1 instanceof Capital && (c2 instanceof Capital || c1 == c2.nation.capital)) ||
                        (c2 instanceof Capital && c1.nation.capital == c2) || (c1.distanceSq(c2) < maxDistanceSq))
                    roadConnections.add(new Map.RoadRoute(cities.get(i), cities.get(j)));
            }

        // Pathfind between cities ranked by the gravity model of migration (https://roadtrees.com/creating-road-trees)
        while (!roadConnections.isEmpty()) {
            Map.RoadRoute current = roadConnections.remove();
            current.path = roadPathfind(current, (current.town1.distanceSq(current.town2) >= maxDistanceSq));
            if (current.path == null) continue;
            Map.addRoadRoute(current);
        }
    }

    private ArrayList<Hex> roadPathfind(Map.RoadRoute connection, boolean useExistingRoads) {
        Hex start = Map.getHex(connection.town1), target = Map.getHex(connection.town2);

        if (connection.town2.size < connection.town1.size) {      // Pathfind from smallest town to largest town, so larger chance of reusing pre-existing roads
            ArrayList<Hex> p = roadPathfind(new Map.RoadRoute(connection.town2, connection.town1), useExistingRoads);
            if (p == null) return null;
            Collections.reverse(p);
            return p;
        }

        //Initialize data structures
        HashMap<Hex,Double> costSoFar = new HashMap<>();
        costSoFar.put(start, 0.0);

        HashMap<Hex,Hex> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        WorldPosition twp = target.toWorldPosition();
        PriorityQueue<Hex> frontier = new PriorityQueue<>(100, Comparator.comparingDouble((Hex hex) -> hex.toWorldPosition().distance(twp)/100 + costSoFar.get(hex)));
        frontier.add(start);

        Hex current;
        while (!frontier.isEmpty()) {   // Standard A* pathfinding
            current = frontier.remove();
            for (Hex next : current.getNeighbours()) {      // For each neighbour
                double newCost = costSoFar.get(current) + getCost(next, current);   // Calculate new cost
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost)) {  // If not processed so far or lower cost
                    costSoFar.put(next, newCost);                                       // Add to data structures
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
            if (current.equals(target)) return buildRoad(connection, cameFrom);     // If found target, build road and end

            if (useExistingRoads) {     // If using existing roads
                Map.RoadRoute route = Map.roadTo(current, connection.town2);
                if (route != null) {            // If road to target exists on target hex
                    ArrayList<Hex> path = buildRoad(current, cameFrom);  // Build road to point, and add existing road
                    path.addAll(route.path.subList(route.path.indexOf(current), route.path.size()));
                    return path;
                }
            }
        }
        return null;
    }

    private ArrayList<Hex> buildRoad(Map.RoadRoute connection, HashMap<Hex, Hex> cameFrom) {
        return buildRoad(Map.getHex(connection.town2), cameFrom);
    }

    private ArrayList<Hex> buildRoad(Hex target, HashMap<Hex, Hex> cameFrom) {
        Hex current = target;
        Stack<Hex> path = new Stack<>();

        do path.push(current);      // Trace back up the route using the cameFrom HashMap, until reaching start (has null source)
        while ((current = cameFrom.get(current)) != null);

        ArrayList<Hex> op = new ArrayList<>();
        while (!path.empty()) op.add(path.pop());   // Reverse the stack onto a list

        // Remove forests along roads
        for (Hex hex : op) {
            if (hex.biome.containsTag(Map.BiomeTag.Forest)) {
                hex.biome = Map.Biome.Grassland;
                for (Hex neighbour : hex.getNeighbours()) if (neighbour.biome.containsTag(Map.BiomeTag.Forest))
                    neighbour.biome = Map.Biome.Grassland;
            }
        }
        return op;
    }

    private double getCost(Hex hex, Hex source) {
        double cost;
        if (hex.biome.getType() != source.biome.getType()) cost = 1000;  // Very high cost of ocean hexes, especially land-ocean borders
        else if (hex.biome.getType() == Map.BiomeTag.Ocean) cost = 100;
        else cost = hex.biome.cost + 10*(source.elevation - hex.elevation)*(source.elevation - hex.elevation);
        if (Double.isNaN(cost)) return 1;                           // Use biome cost and height difference
        return (source.isDestination(hex) ? 0.4 : 1) * cost;     // Prioritize existing roads
    }

    private static final int K = 5, maxPoints = 100;
    private static final double R = 10;
    public void placeTrees() {
        ArrayList<Tree> trees = new ArrayList<>();
        for (int x = 0; x < mapSize.width; x++) for (int y = 0; y < mapSize.height; y++) {
            Hex hex = Map.getHex(x, y);                             // For each forest hex
            if (hex.biome.containsTag(Map.BiomeTag.Forest)) {

                // Use Poisson Disc algorithm by Robert Bridson https://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
                ArrayList<Tree> hexTrees = new ArrayList<>();
                LinkedList<Tree> active = new LinkedList<>();

                WorldPosition centre = hex.toWorldPosition();
                Tree initial = new Tree(centre, hex.biome);
                active.add(initial);
                hexTrees.add(initial);

                while (!active.isEmpty() && hexTrees.size() < maxPoints) {      // While still points to process
                    int index = rand.nextInt(active.size());
                    WorldPosition picked = active.get(index);       // Select random border point
                    boolean found = false;

                    for (int k = 0; k < K; k++) {       // Take K attempts
                        WorldPosition pos = pickPos(picked, R);     // Pick point uniformly in ring between R and 2R
                        double maxR = Game.hexSide * (coastal(hex) ? 1 : 1.5);     // Have trees slightly overlap, but not into water
                        boolean tooClose = tooClose(hexTrees, pos, hex, maxR);
                        if (!tooClose) {             // If point is within maxR and far enough away from each other tree (disregard other hexes' trees for efficiency)
                            found = true;
                            Tree tree = new Tree(pos, hex.biome);
                            active.add(tree);               // Add point
                            hexTrees.add(tree);
                        }
                    }

                    if (!found) active.remove(index);       // If no points were found surrounding the picked point and remove from active
                }
                trees.addAll(hexTrees);
            }
        }
        Map.trees = trees;
    }

    private boolean coastal(Hex hex) {
        for (Hex neighbour : hex.getNeighbours())
            if (neighbour.biome.getType() == Map.BiomeTag.Ocean) return true;
        return false;
    }

    // https://ridlow.wordpress.com/2014/10/22/uniform-random-points-in-disk-annulus-ring-cylinder-and-sphere/
    private WorldPosition pickPos(WorldPosition centre, double R, double r) {   // Picks random point in ring between r and R
        double radius = Math.sqrt(r*r + (R*R-r*r) * rand.nextDouble()), angle = rand.nextDouble() * 2 * Math.PI;
        return new WorldPosition(centre.x + Math.cos(angle)*radius, centre.y + Math.sin(angle)*radius);
    }

    private WorldPosition pickPos(WorldPosition centre, double R) {     // Uniformly picks random point in ring between R and 2R
        double radius = Math.sqrt(1 + 3*rand.nextDouble())*R, angle = rand.nextDouble() * 2 * Math.PI;
        return new WorldPosition(centre.x + Math.cos(angle)*radius, centre.y + Math.sin(angle)*radius);
    }

    private boolean tooClose(ArrayList<Tree> trees, WorldPosition pos, Hex hex, double maxR) {
        if (hex.toWorldPosition().getDistanceSq(pos) > maxR*maxR) return true;
        for (WorldPosition other : trees)
            if (pos.getDistanceSq(other) < R*R) return true;
        return false;
    }
}
