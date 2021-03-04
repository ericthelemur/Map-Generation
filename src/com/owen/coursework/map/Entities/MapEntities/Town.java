package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.map.Entities.Entity;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Town extends Entity {
    public String name;
    public MapLabel label;
    public int size;
    public int influence;
    public Nation nation;

    public Town(int x, int y, String name, int size) {
        super(x, y, null, false);
        this.name = name;
        this.size = size;
        influence = TownType.TOWN.influence;
        label = new CityLabel(this);
        Map.mapLabels.add(label);
    }

    @Override
    public void draw(Graphics2D g) {
        WorldPosition wp = toWorldPosition();
        g.setColor(Color.blue);
        int r = nation != null ? (int) (40.0* (double) size / nation.capital.size) : 5;
        g.fill(new Rectangle2D.Double(wp.x-r, wp.y-r, r*2, r*2));
    }

    public void setName(String name) {
        this.name = name;
        label.text = name;
    }

    @Override
    public String toString() {
        return "Town " + name +
                ", at (" + x +
                "," + y +
                ')';
    }

    public enum TownType {
        TOWN(20),
        CAPITAL(50);

        public int influence;

        TownType(int influence) {
            this.influence = influence;
        }
    }
}
