package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.Utilities;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Tree extends WorldPosition {
    private Map.Biome biome;

    private final Color trunkColour = new Color(100, 50, 25);

    public Tree(double x, double y, Map.Biome biome) {
        super(x, y);
        this.biome = biome;
    }

    public Tree(WorldPosition wp, Map.Biome biome) {
        super(wp);
        this.biome = biome;
    }

    public void draw(Graphics2D g) {
        g.setColor(trunkColour);
        g.fill(new Rectangle2D.Double(x-1, y+2, 2, 6));

        Color biomeColour = biome.colour;
        g.setColor(new Color(biomeColour.getRed() - 30, biomeColour.getGreen() - 10, biomeColour.getBlue() - 25));
        Utilities.fillCircle(this, 5, g);
    }
}
