package com.owen.coursework.ui;

import com.owen.coursework.Listener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Button extends Label {

    private Runnable function;
    private Color hoverColour = Color.LIGHT_GRAY, clickColour = Color.GRAY, backgroundColour = Color.WHITE;

    public Button(String text, Runnable function, Color textColour, Color backgroundColour, Color hoverColour,
                  Color clickColour, Font font, Anchor anchor, int x, int y, int width, int height, int border) {
        super(text, textColour, backgroundColour, font, anchor, x, y, width, height, border);
        this.function = function;
        this.hoverColour = hoverColour;
        this.clickColour = clickColour;
    }

    public Button(String text, Runnable function, Anchor anchor, int x, int y, int width, int height) {
        super(text, anchor, x, y, width);
        size.height = height;
        this.function = function;
    }

    public Button(String text, Runnable function, Anchor anchor, int x, int y) {
        super(text, anchor, x, y);
        this.size.width += 10;
        this.size.height += 10;
        this.function = function;
    }

    public Button(String text, Runnable function) {
        super(text, Anchor.MiddleMiddle, 0, 0);
        this.function = function;
    }

    @Override
    public void manageControls(double timePassed) {
        super.manageControls(timePassed);
        Rectangle rect = getRect();
        if (rect.contains(Listener.getMousePos()) && Listener.UIButtonReleased(MouseEvent.BUTTON1) && function != null)
            function.run();
    }

    @Override
    public void draw(Graphics2D g) {
        Rectangle rect = getRect();

        g.setColor(Color.WHITE);
        if (backgroundColour != null) g.setColor(backgroundColour);
        if (hoverColour != null && rect.contains(Listener.getMousePos())) g.setColor(hoverColour);
        if (clickColour != null && rect.contains(Listener.getMousePos()) && Listener.buttonHeld(0)) g.setColor(clickColour);

        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        g.setColor(Color.BLACK);
        if (textColour != null) g.setColor(textColour);
        if (font != null) g.setFont(font);
        drawText(g);
    }
}
