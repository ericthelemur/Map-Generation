package com.owen.coursework.ui;

import com.owen.coursework.Listener;
import com.owen.coursework.map.Position.ScreenPosition;

import java.awt.*;

public class UIElement {
    protected Anchor anchor;
    protected ScreenPosition offset = new ScreenPosition(0, 0);
    protected Dimension size = new Dimension(100, 100);
    protected Color backgroundColour;

    // Constructors
    public UIElement(Anchor anchor, int x, int y, int width, int height) {
        this.anchor = anchor;
        this.offset.x = x;
        this.offset.y = y;
        this.size.width = width;
        this.size.height = height;
    }

    public UIElement(Color color, Anchor anchor, int x, int y, int width, int height) {
        this(anchor, x, y, width, height);
        this.backgroundColour = color;
    }

    public void manageControls(double timePassed) {     // Sets if mouse is on UI element, not map
        if (getRect().contains(Listener.getMousePos())) Listener.setMouseOnUI(true);
    }

    public void update(double timePassed) {}

    public void draw(Graphics2D g) {
        if (backgroundColour != null) {
            g.setColor(backgroundColour);
            g.fillRect(getX(), getY(), size.width, size.height);
        }
    }

    public Rectangle getRect() {
        return new Rectangle(getX(), getY(), size.width, size.height);
    }

    public int getX() {
        return anchor.getX(size.width) + offset.x;
    }

    public int getY() {
        return anchor.getY(size.height) + offset.y;
    }

    public UIElement setBackgroundColour(Color backgroundColour) {
        this.backgroundColour = backgroundColour;
        return this;
    }

    public ScreenPosition getOffset() {
        return offset;
    }

    public UIElement setOffset(ScreenPosition offset) {
        this.offset = offset;
        return this;
    }

    public UIElement addOffset(ScreenPosition offset) {
        this.offset.add(offset);
        return this;
    }

    public UIElement setSize(Dimension size) {
        this.size = size;
        return this;
    }
    
    public UIElement setWidth(int width) {
        size.width = width;
        return this;
    }

    public UIElement setHeight(int height) {
        size.height = height;
        return this;
    }

    @Override
    public String toString() {
        return "UIElement{" +
                "offset=" + offset +
                ", size=" + size +
                ", backgroundColour=" + backgroundColour +
                '}';
    }
}
