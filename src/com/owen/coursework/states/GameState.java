package com.owen.coursework.states;

import com.owen.coursework.Camera;
import com.owen.coursework.Game;
import com.owen.coursework.Listener;
import com.owen.coursework.Utilities;
import com.owen.coursework.map.Hex;
import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.MapPosition;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.owen.coursework.ui.*;
import com.owen.coursework.ui.Button;
import com.owen.coursework.ui.Label;

public class GameState implements BlankState {
    private final static Logger logger = Logger.getLogger(Game.class.getName());
    public Camera camera;
    private final int MOUSE_PUSH_BORDER = 5;

    private ArrayList<UIElement> uiElements = new ArrayList<>();
    private ArrayList<UIElement> debugUI = new ArrayList<>();

    public GameState() {
        camera = new Camera(0, 0, 1, Game.hexSide, 0.01, 1, 0.7, 0.25); //1.2

        // Create UI Elements
        uiElements.add(new ElementArray(Anchor.BottomMiddle, 0, -20, 10, ElementArray.ORIENTATION_HORIZONTAL,
            new Button("Toggle Debug", Game::toggleDebug),
            new Button("Reload Map", Map::updateMapNow),
            new Button("Save Map Image", Map::saveMap)
        ));

        ArrayList<UIElement> buttons = new ArrayList<>();       // Construct debug screen buttons
        for (Map.dispModes dm : Map.dispModes.values()) buttons.add(new Button(dm.name(), () -> Map.setDispMode(dm)));

        debugUI.add(new ElementArray(Anchor.MiddleLeft, 20, 0, 10, ElementArray.ORIENTATION_HORIZONTAL,
                    new ElementArray(buttons)));
    }

    @Override
    public void onTransfer() {}

    @Override
    public void manageControls(double millisPassed) {
        for (UIElement element : uiElements) element.manageControls(millisPassed);  // Updates UI elements
        if (Game.debug) for (UIElement element : debugUI) element.manageControls(millisPassed);

        int msInt = (int) millisPassed * 10;

        // WASD and arrow keys
        if (Listener.isHeld(KeyEvent.VK_LEFT) || Listener.isHeld(KeyEvent.VK_A))  camera.addOffset(-msInt, 0);
        if (Listener.isHeld(KeyEvent.VK_RIGHT) || Listener.isHeld(KeyEvent.VK_D)) camera.addOffset(msInt, 0);
        if (Listener.isHeld(KeyEvent.VK_DOWN) || Listener.isHeld(KeyEvent.VK_S))  camera.addOffset(0, msInt);
        if (Listener.isHeld(KeyEvent.VK_UP) || Listener.isHeld(KeyEvent.VK_W))    camera.addOffset(0, -msInt);
        if (Listener.getWheelRotation() != 0) camera.zoom(-Listener.getWheelRotation());

        // Mouse movement
        if (Listener.getMousePos().x < MOUSE_PUSH_BORDER)                               camera.addOffset(-msInt, 0);
        if (Listener.getMousePos().x > Game.screenSize.width - MOUSE_PUSH_BORDER)  camera.addOffset(msInt, 0);
        if (Listener.getMousePos().y < MOUSE_PUSH_BORDER)                               camera.addOffset(0, -msInt);
        if (Listener.getMousePos().y > Game.screenSize.height - MOUSE_PUSH_BORDER)  camera.addOffset(0, msInt);
        if (camera.hasChanged()) camera.checkLimits();

        if (Listener.buttonPressed(MouseEvent.BUTTON1) && Listener.getMousePos().toMapPosition() != null) Map.selected = Map.getHex(Listener.getMousePos().toMapPosition());

        if (Game.debug) {
            if (Listener.isReleased(KeyEvent.VK_CLOSE_BRACKET)) Map.dispMode = Map.dispMode.next();
            if (Listener.isReleased(KeyEvent.VK_OPEN_BRACKET)) Map.dispMode = Map.dispMode.prev();
        }
    }

    @Override
    public void update(double timePassed) {
        for (UIElement element : uiElements) element.update(timePassed);
        if (Game.debug) for (UIElement element : debugUI) element.update(timePassed);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Map.backgroundColour);
        g.fillRect(0, 0, Game.screenSize.width, Game.screenSize.height);
        g.setStroke(new BasicStroke(5));

        Map.draw(g);

        for (UIElement element : uiElements) element.draw(g);

        if (Game.debug) debugDraw(g);
    }

    private void debugDraw(Graphics2D g) {      // Draws debug text in top-left corner
        for (UIElement element : debugUI) element.draw(g);

        g.setFont(Game.Fonts.smallFont);
        MapPosition mp = Listener.getMousePos().toMapPosition();
        Hex hex = null;
        if (mp == null) mp = new MapPosition(-1, -1);
        else hex = Map.getHex(mp);

        g.setColor(Color.WHITE);
        Utilities.drawMultipleLines(String.format("Seed: %d Display Mode: %s\nCamera: (%.2f %.2f %.2f %.2f) Zoom: %.3f\nBounds: (%.2f %.2f %.2f %.2f)\nMouse: Screen: (%d %d) World: (%.2f %.2f) Map: (%d %d)\n%s\nMouseOnUI: %b",
                Game.seed, Map.dispMode, camera.view.x, camera.view.y, camera.view.width, camera.view.height, camera.getZoom(),
                Map.bounds.x, Map.bounds.y, Map.bounds.width, Map.bounds.height,
                Listener.getMousePos().x, Listener.getMousePos().y, Listener.getMousePos().toWorldPosition().x, Listener.getMousePos().toWorldPosition().y, mp.x, mp.y,
                    hex == null ? "" : (Label.wrapText(hex.toString(), (int) (Game.screenSize.width*0.9), null) + Label.wrapText(hex.destinations.toString(), (int) (Game.screenSize.width*0.9), null)),
                Listener.isMouseOnUI()
        ), 10, 30, g);
    }
}
