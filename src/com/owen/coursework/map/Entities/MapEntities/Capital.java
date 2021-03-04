package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Capital extends Town {

    public Capital(int x, int y, int size, Nation nation) {
        super(x, y, nation.generateName(), size);
        influence = Town.TownType.CAPITAL.influence;
        this.nation = nation;
    }

    public Capital(MapPosition mp, int size, Nation nation) {
        this(mp.x, mp.y, size, nation);
    }

    @Override
    public void draw(Graphics2D g) {
        WorldPosition wp = toWorldPosition();
        g.setColor(Color.red);
        g.fill(new Rectangle2D.Double(wp.x-15, wp.y-15, 30, 30));
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        return "Capital " + name +
                ", at (" + x +
                "," + y +
                ')';
    }
}
