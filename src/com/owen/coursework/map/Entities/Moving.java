package com.owen.coursework.map.Entities;

import com.owen.coursework.Utilities;
import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Moving extends Entity {
    private MapPosition target;
    private Queue<MapPosition> route;
    protected double speed = 1;

    public Moving(int x, int y, BufferedImage sprite) {
        super(x, y, sprite);
    }

    public void update(double millisPassed) {}

    public Queue<MapPosition> pathfind(MapPosition endPos) {
        return pathfind(this, endPos);
    }

    public static Queue<MapPosition> pathfind(MapPosition start, MapPosition target) {
        WorldPosition twp = Map.getHex(target).toWorldPosition();

        if (Map.getHex(target).biome.cost == 0) {
            System.out.println("Searching Cancelled, Target is Unreachable");
            return null;
        }

        HashMap<MapPosition,Integer> costSoFar = new HashMap<>();
        costSoFar.put(start, 0);

        HashMap<MapPosition,MapPosition> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        PriorityQueue<MapPosition> frontier = new PriorityQueue<>(100, Comparator.comparingDouble((MapPosition mp) -> mp.toWorldPosition().distance(twp)/200 + costSoFar.get(mp)));
        frontier.add(start);

        MapPosition current;
        while (frontier.size() > 0) {
            current = frontier.remove();
            for (MapPosition next : Map.getHex(current).getNeighboursPos()) {
                int newCost = costSoFar.get(current) + Map.getHex(next).biome.cost;
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost) && Map.getHex(next).biome.cost != 0) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
            if (current.equals(target)) return constructPath(start, target, cameFrom);
        }
        return null;
    }

    private static Queue<MapPosition> constructPath(MapPosition start, MapPosition target, HashMap<MapPosition, MapPosition> cameFrom) {
        MapPosition current = target;
        Stack<MapPosition> path = new Stack<>();
        while (!(current.equals(start))) {
            path.push(current);
            current = cameFrom.get(current);
            if (current == null) break;
        }
        Queue<MapPosition> op = new LinkedList<>();
        while (!path.empty()) op.add(path.pop());
        return op;
    }

    @Override
    public void draw(Graphics2D g) {
        if (route != null && route.size() > 0) drawRoute(g);
        super.draw(g);
    }

    private void drawRoute(Graphics2D g) {
        g.setColor(colour);
        ArrayList<MapPosition> routeArr = getRouteArr();
        WorldPosition wp1 = Map.getHex(this).toWorldPosition(), wp2 = Map.getHex(routeArr.get(0)).toWorldPosition();
        Utilities.drawLine(wp1, wp2, g);

        for (int i = 0; i < routeArr.size() - 1; i++) {
            wp1 = Map.getHex(routeArr.get(i)).toWorldPosition();
            wp2 = Map.getHex(routeArr.get(i+1)).toWorldPosition();

            Utilities.drawLine(wp1, wp2, g);
            Utilities.fillCircle(wp1, 10, g);
        }
        WorldPosition endPos = routeArr.get(routeArr.size()-1).toWorldPosition();
        Utilities.drawLine(WorldPosition.add(endPos, 10, 10), WorldPosition.add(endPos, -10, -10), g);
        Utilities.drawLine(WorldPosition.add(endPos, 10, -10), WorldPosition.add(endPos, -10, 10), g);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        if (target != null) route = pathfind(target);
    }

    @Override
    public void setLocation(MapPosition mp) {
        if (mp == null) return;
        setLocation(mp.x, mp.y);
    }

    public MapPosition getTarget() {
        return target;
    }

    public void setTarget(MapPosition target) {
        this.target = target;
        route = pathfind(target);
    }

    public Queue<MapPosition> getRoute() {
        return route;
    }

    public void setRoute(Queue<MapPosition> route) {
        this.route = route;
    }

    public ArrayList<MapPosition> getRouteArr() {
        if (route == null || route.size() == 0) return null;
        return new ArrayList<>(route);
    }

    public void setRoute(ArrayList<MapPosition> route) {
        this.route = new LinkedList<>(route);
    }

    public Hex getHex() {
        return Map.getHex(this);
    }
}
