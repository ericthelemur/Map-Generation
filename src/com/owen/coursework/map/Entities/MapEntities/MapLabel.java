package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.Game;
import com.owen.coursework.map.Position.ScreenPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;

public class MapLabel {
    public String text;
    public WorldPosition worldPosition;
    public ScreenPosition offset = new ScreenPosition(0, -5);
    public int priority = 0;
    Font font = Game.Fonts.smallFont;

    public MapLabel(String text, WorldPosition worldPosition, ScreenPosition offset, int priority, Font font) {
        this.text = text;
        this.worldPosition = worldPosition;
        this.offset = offset;
        this.priority = priority;
        this.font = font;
    }

    public MapLabel(String text, WorldPosition worldPosition) {
        this.text = text;
        this.worldPosition = worldPosition;
    }

    public ScreenPosition getScreenPosition() {
        return ScreenPosition.add(worldPosition.toScreenPosition(), offset);
    }

    public Rectangle getBoundingRect(Graphics2D g) {
        FontMetrics metrics = g.getFontMetrics(font);
        ScreenPosition sp = worldPosition.toScreenPosition();
        int w = metrics.stringWidth(text), h = metrics.getHeight();
        return new Rectangle(sp.x - w/2, sp.y - h/2, w, h);
    }

    public Font getFont() {
        return font;
    }

    @Override
    public String toString() {
        return "MapLabel{" +
                "text: '" + text + '\'' +
                " at (" + worldPosition.x + "," + worldPosition.y +
                "), offset (" + offset.x + "," + offset.y +
                "), P: " + priority +
                '}';
    }
}
