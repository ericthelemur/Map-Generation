package com.owen.coursework.map;

import com.owen.coursework.Game;
import com.owen.coursework.map.Entities.MapEntities.Town;
import com.owen.coursework.map.Entities.MapEntities.Nation;
import com.owen.coursework.map.Position.DoubleDimension;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class Hex extends MapPosition {
    private static int side = Game.hexSide;
    private static DoubleDimension size = Game.hexSize;

    private static Path2D.Double points = new Path2D.Double();
    static {{       // Create polygon
        double r = side + 0.27;
        points.moveTo(0, r);
        for (double d = 0; d < Math.PI*2; d += Math.PI/3) points.lineTo(r*Math.sin(d), r*Math.cos(d));
        points.closePath();
    }}

    private Path2D.Double p;
    public double elevation = 0, rainfall = 0, gradient = 0, cityChance = 0, waterElevation = -1, waterFlux = rainfall;
    public ArrayList<Road> roads = new ArrayList<>();
    public ArrayList<Town> destinations = new ArrayList<>();
    public Hex flow;
    public ArrayList<Hex> sources = new ArrayList<>(2);
    public Map.Area area;
    public Map.Biome biome = Map.Biome.Blank;
    public Town town;
    public Nation nation;

    public Hex(int x, int y) {
        super(x, y);
        WorldPosition wp = toWorldPosition();
        
        // Set up hex polygon with double precision
        AffineTransform at = new AffineTransform();
        at.translate(wp.x, wp.y);
        p = new Path2D.Double(points, at);
    }

    public Hex(MapPosition mp) {
        this(mp.x, mp.y);
    }

    public void fill(Graphics2D g) {
        g.fill(p);
    }

    public void draw(Graphics2D g) {
        g.draw(p);
    }

    public Path2D.Double getShape() {
        return p;
    }

    public enum HexDirection {
        EAST(+1, 0, +1, 0), NORTHEAST(0, -1, +1, -1), NORTHWEST(-1, -1, 0, -1),
        WEST(-1, 0, -1, 0), SOUTHWEST(-1, +1, 0, +1), SOUTHEAST(0, +1, +1, +1);

        MapPosition even, odd;

        HexDirection(int evenX, int evenY, int oddX, int oddY) {
            even = new MapPosition(evenX, evenY);
            odd = new MapPosition(oddX, oddY);
        }

        public MapPosition getDir(int y) {
            return (y % 2 == 0) ? even : odd;
        }
    }

    public MapPosition getNeighbourPos(HexDirection dir) {
        return MapPosition.add(this, dir.getDir(y));
    }

    public Hex getNeighbour(HexDirection dir) {
        MapPosition mp = getNeighbourPos(dir);
        if ((mp.x >= 0) && (mp.x < Map.size.width) && (mp.y >= 0) && (mp.y < Map.size.height))
            return Map.getHex(getNeighbourPos(dir));
        else return null;
    }

    public ArrayList<MapPosition> getNeighboursPos() {
        ArrayList<MapPosition> neighbours = new ArrayList<>();
        for (HexDirection dir : HexDirection.values()) {
            MapPosition neightbourPos = getNeighbourPos(dir);
            if (new Rectangle(Map.size).contains(neightbourPos)) neighbours.add(neightbourPos);
        }
        return neighbours;
    }

    public ArrayList<Hex> getNeighbours() {
        ArrayList<Hex> neighbours = new ArrayList<>();
        for (MapPosition pos : getNeighboursPos()) neighbours.add(Map.getHex(pos));
        return neighbours;
    }

    public ArrayList<Hex> getRing(int N) {
        MapPosition point = new MapPosition(this);
        ArrayList<Hex> result = new ArrayList<>();
        for (int i = 0; i < N; i++) point.add(HexDirection.SOUTHWEST.getDir(point.y));

        for (HexDirection dir : HexDirection.values()) for (int i = 0; i < N; i++) {
            if ((point.x >= 0) && (point.x < Map.size.width) && (point.y >= 0) && (point.y <Map.size.height)) {
                result.add(Map.getHex(point));
                point = Map.getHex(point).getNeighbourPos(dir);
            }
        }
        return result;
    }

    public ArrayList<Hex> getRings(int N) {
        ArrayList<Hex> result = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            result.addAll(getRing(i));
        }
        return result;
    }

    public WorldPosition getTopLeft() {
        WorldPosition wp = toWorldPosition();
        return new WorldPosition((int) (wp.x-size.width/2), (int) (wp.y-size.height/2));
    }

    public double getWaterElevation() {
        if (waterElevation == -1) return Math.max(elevation, Map.seaLevel);
        return waterElevation;
    }

    public boolean isRiver() {
        return waterFlux > Map.maxRiverSize * 0.02;
    }

    public boolean isRoad() {
        return roads.size() > 0;
    }

    public void addRoad(Road road, Town town1, Town town2) {
        if (!destinations.contains(town1)) destinations.add(town1);
        if (!destinations.contains(town2)) destinations.add(town2);
        for (Road road1 : roads) {
            if (road.to == road1.to) {
                road1.size += road.size;
                return;
            }
        }
        roads.add(road);
    }

    public boolean isDestination(Town town) {
        return destinations.contains(town);
    }

    public boolean isDestination(Hex hex) {
        for (Road road : roads) {
            if (road.to == hex) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "Hex{" +
                "x=" + x +
                ", y=" + y +
//                ", p=" + p +
                String.format(", elevation=%.2f, rainFall=%.2f, waterFlux=%.2f", elevation, rainfall, waterFlux) +
//                ", gradient=" + gradient +
//                ", cityChance=" + cityChance +
//                ", waterElevation=" + waterElevation +
//                ", roads=" + roads +
//                ", roadTo=" + roadTo +
                ", flow=("+(flow == null ? "null" : (flow.x+","+flow.y))+")" +
                ", area=" + area +
                ", biome=" + biome +
                ", town=" + town +
                ", nation=" + nation +
                '}';
    }

    public static class Road {
        public Hex to;
        public float size;

        public Road(Hex to, float size) {
            this.to = to;
            this.size = size;
        }


        @Override
        public String toString() {
            return "Road{" +
                    "size=" + size +
                    '}';
        }
    }
}
