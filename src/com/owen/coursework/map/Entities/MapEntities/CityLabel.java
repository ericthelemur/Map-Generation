package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.Game;
import com.owen.coursework.map.Position.ScreenPosition;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;

public class CityLabel extends MapLabel {
    public CityLabel(Town town, ScreenPosition offset, int priority) {
        super(town.name, town.toWorldPosition(), offset, priority, Game.Fonts.XSFont);
    }

    public CityLabel(String name, WorldPosition pos, ScreenPosition offset, int priority, Font font) {
        super(name, pos, offset, priority, font);
    }

    public CityLabel(Town town) {
        super(town.name, town.toWorldPosition().sub(0, (int) (Game.hexSize.height / 4)), new ScreenPosition(0, -10), 0, Game.Fonts.XSFont);
        if (town.getClass().equals(Capital.class)) {
            priority = 1;
            font = Game.Fonts.mediumFont;
        }
    }
}
