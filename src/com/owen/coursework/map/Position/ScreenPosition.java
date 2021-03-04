package com.owen.coursework.map.Position;


import com.owen.coursework.Camera;
import com.owen.coursework.Game;

import java.awt.*;

public class ScreenPosition extends Point {
    public ScreenPosition(int x, int y) {
        super(x, y);
    }

    public WorldPosition toWorldPosition() {
        Camera c = Game.States.GameState.camera;
        return new WorldPosition((int) ((double) x/c.getZoom() + c.view.x), (int) ((double) y/c.getZoom() + c.view.y));
    }

    public MapPosition toMapPosition() {
        return toWorldPosition().toMapPosition();
    }

    public ScreenPosition add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public ScreenPosition add(ScreenPosition pos) {
        return add(pos.x, pos.y);
    }

    public ScreenPosition sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public ScreenPosition sub(ScreenPosition pos) {
        return sub(pos.x, pos.y);
    }

    public static ScreenPosition add(int x1, int y1, int x2, int y2) {
        return new ScreenPosition(x1+x2, y1+y2);
    }

    public static ScreenPosition add(ScreenPosition pos1, ScreenPosition pos2) {
        return new ScreenPosition(pos1.x+pos2.x, pos1.y+pos2.y);
    }

    public static ScreenPosition sub(int x1, int y1, int x2, int y2) {
        return new ScreenPosition(x1-x2, y1-y2);
    }

    public static ScreenPosition sub(ScreenPosition pos1, ScreenPosition pos2) {
        return new ScreenPosition(pos1.x-pos2.x, pos1.y-pos2.y);
    }
}
