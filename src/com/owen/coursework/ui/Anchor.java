package com.owen.coursework.ui;

import com.owen.coursework.Game;
import com.owen.coursework.map.Position.ScreenPosition;

import java.awt.*;

public class Anchor {
    public static final Anchor TopLeft = new Anchor(0, 0),
            TopMiddle = new Anchor(0.5, 0),
            TopRight = new Anchor(1, 0),
            MiddleLeft = new Anchor(0, 0.5),
            MiddleMiddle = new Anchor(0.5, 0.5),
            MiddleRight = new Anchor(1, 0.5),
            BottomLeft = new Anchor( 0, 1),
            BottomMiddle = new Anchor(0.5, 1),
            BottomRight = new Anchor(1, 1);

    private final double widthFraction, heightFraction;
    private ScreenPosition position = new ScreenPosition(0, 0);
    private Dimension fullDimensions;

    Anchor(double widthFraction, double heightFraction) {
        this.widthFraction = widthFraction;
        this.heightFraction = heightFraction;
    }

    Anchor(double widthFraction, double heightFraction, ScreenPosition position, Dimension fullDimensions) {
        this.widthFraction = widthFraction;
        this.heightFraction = heightFraction;
        this.position = position;
        this.fullDimensions = fullDimensions;
    }

    public int getX() {
        return position.x + (int) ((double) (fullDimensions != null ? fullDimensions : Game.screenSize).width*widthFraction);
    }

    public int getX(int objWidth) {
        return position.x + (int) ((double) ((fullDimensions != null ? fullDimensions : Game.screenSize).width-objWidth)*widthFraction);
    }

    public int getY() {
        return position.y + (int) ((double) (fullDimensions != null ? fullDimensions : Game.screenSize).height*heightFraction);
    }

    public int getY(int objHeight) {
        return position.y + (int) ((double) ((fullDimensions != null ? fullDimensions : Game.screenSize).height-objHeight)*heightFraction);
    }

    public double getWidthFraction() {
        return widthFraction;
    }

    public double getHeightFraction() {
        return heightFraction;
    }
}
