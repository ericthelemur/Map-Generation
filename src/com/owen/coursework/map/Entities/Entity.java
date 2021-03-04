package com.owen.coursework.map.Entities;

import com.owen.coursework.Game;
import com.owen.coursework.Utilities;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class Entity extends MapPosition {
    private final static Logger logger = Logger.getLogger(Game.class.getName());
    private BufferedImage sprite;
    protected Color colour;

    public Entity(int x, int y, BufferedImage sprite) {
        this(x, y, sprite, true);
    }

    public Entity(int x, int y, BufferedImage sprite, boolean output) {
        super(x, y);
        if (output) logger.info("Created entity " + this.toString());
        this.sprite = sprite;
        colour = new Color(Game.rand.nextInt(256), Game.rand.nextInt(256), Game.rand.nextInt(256));
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void draw(Graphics2D g) {
        WorldPosition wp = toWorldPosition();
        g.setColor(colour);
        Utilities.fillCircle(wp, 20, g);
    }

    public void setLocation(int x, int y) {
        if (!new Rectangle(Map.size).contains(x, y)) return;
        this.x = x;
        this.y = y;
    }

    public void setLocation(MapPosition mp) {
        setLocation(mp.x, mp.y);
    }
}
