package com.owen.coursework.map.Position;

import com.owen.coursework.Camera;
import com.owen.coursework.Game;
import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class WorldPosition extends Point2D.Double {
    public WorldPosition(double x, double y) {
        super(x, y);
    }

    public WorldPosition(WorldPosition wp) {
        super(wp.x, wp.y);
    }

    public MapPosition toMapPosition() {
        WorldPosition approx = new WorldPosition(x/Game.hexSize.width, y/Game.hexSize.height);
        for (Hex hex : Map.queryRange(new Rectangle2D.Double(approx.x-3, approx.y-3, approx.x+3, approx.y+3))) {
            if (hex.getShape().contains(x, y)) return new MapPosition(hex.x, hex.y);
        }
        return null;
    }

    public ScreenPosition toScreenPosition() {
        Camera c = Game.States.GameState.camera;
        return new ScreenPosition((int) ((x-c.view.x)*c.getZoom()), (int) ((y-c.view.y)*c.getZoom()));
    }

    public double getDistanceSq(WorldPosition wp2) {
        return getDistanceSq(wp2.x, wp2.y);
    }

    public double getDistanceSq(double x, double y) {
        return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y);
    }

    public WorldPosition add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public WorldPosition add(WorldPosition pos) {
        return add(pos.x, pos.y);
    }

    public WorldPosition sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public WorldPosition sub(WorldPosition pos) {
        return sub(pos.x, pos.y);
    }


    public static WorldPosition add(double x1, double y1, double x2, double y2) {
        return new WorldPosition(x1+x2, y1+y2);
    }

    public static WorldPosition add(WorldPosition pos1, WorldPosition pos2) {
        return new WorldPosition(pos1.x+pos2.x, pos1.y+pos2.y);
    }

    public static WorldPosition add(WorldPosition pos1, double x, double y) {
        return new WorldPosition(pos1.x+x, pos1.y+y);
    }

    public static WorldPosition sub(double x1, double y1, double x2, double y2) {
        return new WorldPosition(x1-x2, y1-y2);
    }

    public static WorldPosition sub(WorldPosition pos1, WorldPosition pos2) {
        return new WorldPosition(pos1.x-pos2.x, pos1.y-pos2.y);
    }

    public int getIntX() {
        return (int) x;
    }

    public int getIntY() {
        return (int) y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return super.equals(obj);
    }
}
