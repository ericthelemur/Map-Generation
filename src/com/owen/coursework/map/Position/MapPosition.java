package com.owen.coursework.map.Position;

import com.owen.coursework.Game;

import java.awt.*;

public class MapPosition extends Point {

    public MapPosition(int x, int y) {
        super(x, y);
    }

    public MapPosition(MapPosition mp) {
        super(mp.x, mp.y);
    }

    public WorldPosition toWorldPosition() {
        if (y%2==0) return new WorldPosition((Game.hexSize.getWidth()*x), (y * Game.hexSize.getHeight()*0.75));
        else  return new WorldPosition((Game.hexSize.getWidth()*(x+0.5)), (y * Game.hexSize.getHeight()*0.75));
    }

    public ScreenPosition toScreenPosition() {
        return toWorldPosition().toScreenPosition();
    }

    public double getDistance(MapPosition mp2) {
        return getDistance(mp2.x, mp2.y);
    }

    public double getDistance(int x, int y) {
        return Math.sqrt(Math.pow(x-this.x, 2) + Math.pow(y-this.y, 2));
    }

    public double getDistanceSq(MapPosition mp2) {
        return getDistanceSq(mp2.x, mp2.y);
    }

    public double getDistanceSq(int x, int y) {
        return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y);
    }

    public MapPosition add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public MapPosition add(MapPosition pos) {
        return add(pos.x, pos.y);
    }

    public MapPosition sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public MapPosition sub(MapPosition pos) {
        return sub(pos.x, pos.y);
    }


    public static MapPosition add(int x1, int y1, int x2, int y2) {
        return new MapPosition(x1+x2, y1+y2);
    }

    public static MapPosition add(MapPosition pos1, MapPosition pos2) {
        return new MapPosition(pos1.x+pos2.x, pos1.y+pos2.y);
    }

    public static MapPosition sub(int x1, int y1, int x2, int y2) {
        return new MapPosition(x1-x2, y1-y2);
    }

    public static MapPosition sub(MapPosition pos1, MapPosition pos2) {
        return new MapPosition(pos1.x-pos2.x, pos1.y-pos2.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapPosition that = (MapPosition) o;
        return this.x == that.x && this.y == that.y;
    }
}
