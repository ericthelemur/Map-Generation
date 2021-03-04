package com.owen.coursework.states;

import com.owen.coursework.Game;
import com.owen.coursework.Listener;

import java.awt.*;

public class LoadState implements BlankState {
    private Color colour = new Color(200, 90, 10);

    @Override
    public void onTransfer() {
        setLoadMessage("Loading...");
    }

    @Override
    public void manageControls(double timePassed) {}

    @Override
    public void update(double timePassed) {}

    @Override
    public void draw(Graphics2D g) {
        drawMessage("Done!", g);
    }

    public void setLoadMessage(String message) {
        Graphics2D g = Game.getGraphics();
        drawMessage(message, g);

        g.dispose();
        Game.show();
    }

    private void drawMessage(String message, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(colour);
        g.fillRect(0, 0, Game.screenSize.width, Game.screenSize.height);

        g.setColor(Color.WHITE);
        g.setFont(Game.Fonts.mediumFont);
        FontMetrics metrics = g.getFontMetrics();
        int x = Game.screenSize.width/2 - metrics.stringWidth(message)/2;
        int y = Game.screenSize.height/2 - metrics.getHeight()/2;
        g.drawString(message, x, y);
    }

    public void finishedLoading() {
        setLoadMessage("Done!");
        Listener.reset();
        Game.previousState();
    }
}
