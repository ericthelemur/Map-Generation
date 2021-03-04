package com.owen.coursework.map.Entities.MapEntities;

import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.NameGenerator.BackOffGenerator;
import com.owen.coursework.map.Position.MapPosition;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class Nation {
    public String name;
    public Capital capital;
    public BackOffGenerator nameGenerator;
    public Color colour;

    public ArrayList<Town> towns = new ArrayList<>();
    public ArrayList<Hex> territory = new ArrayList<>(), border = new ArrayList<>();
    public Path2D.Double borderShape;

    public Nation(MapPosition capitalPos, int capSize, BackOffGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
        this.capital = new Capital(capitalPos, capSize, this);
        name = capital.name + "ia";
        territory.add(Map.getHex(capital));
    }

    public String generateName() {
        return nameGenerator.generate();
    }

    public void addTerritory(Hex hex) {
        if (!territory.contains(hex)) territory.add(hex);
        if (hex.town != null) towns.add(hex.town);
    }

    public void addBorder(Hex hex) {
        if (!border.contains(hex)) border.add(hex);
    }

    @Override
    public String toString() {
        return "Nation{" +
                "name='" + name + '\'' +
                ", capital=" + capital +
                ", nameGenerator=" + nameGenerator +
                '}';
    }
}
