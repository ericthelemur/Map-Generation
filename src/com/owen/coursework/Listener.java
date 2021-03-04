package com.owen.coursework;

import com.owen.coursework.map.Position.ScreenPosition;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.BitSet;

public class Listener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, WindowListener {
    // Stores key and button presses
    private static BitSet keysPressed = new BitSet(255), keysHeld = new BitSet(255), keysReleased = new BitSet(255);
    private static BitSet buttonsHeld = new BitSet(4), buttonsPressed = new BitSet(4), buttonsReleased = new BitSet(4);
    private static ScreenPosition mousePos = new ScreenPosition(0, 0);
    private static int wheelRotation = 0;

    private static boolean mouseOnUI = false;

    public static void reset() {        // Resets at the end of each game loop
        keysPressed.set(0, keysPressed.size(), false);
        keysReleased.set(0, keysReleased.size(), false);
        buttonsPressed.set(0, buttonsPressed.size(), false);
        buttonsReleased.set(0, buttonsPressed.size(), false);

        wheelRotation = 0;
    }

    // Useful handlers
    @Override
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        try {
            if (!keysHeld.get(kc)) keysPressed.set(kc, true);   // If this is the first frame that the key has been pressed
            keysHeld.set(kc, true);                             // Set key pressed, then set key held
        } catch (IndexOutOfBoundsException ignored) {}
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            keysHeld.set(e.getKeyCode(), false);            // Reset keys held
            keysReleased.set(e.getKeyCode(), true);         // Sets released
        } catch (IndexOutOfBoundsException ignored) {}
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int status = e.getID(), key = e.getButton();
        if(status == MouseEvent.MOUSE_PRESSED) {
            if (!buttonsHeld.get(key)) buttonsPressed.set(key, true);   // Sets button on press
            buttonsHeld.set(key, true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int status = e.getID(), key = e.getButton();
        if(status == MouseEvent.MOUSE_RELEASED) {                   // set release on release event
            if (buttonsHeld.get(key)) buttonsReleased.set(key, true);
            buttonsHeld.set(key, false);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {        // Update mousePos on mouse movement
        mousePos.x = e.getX();
        mousePos.y = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {          // Update mousePos on mouse movement
        mousePos.x = e.getX();
        mousePos.y = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        wheelRotation = e.getWheelRotation();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Game.stop();
    }   // Stops program on close


    public static boolean isPressed(int kc) {       // Returns key pressed value
        try { return keysPressed.get(kc);
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    public static ArrayList<Integer> getPressed() {         // Get a list of all keys pressed, for digit box
        ArrayList<Integer> pressed = new ArrayList<>();
        for (int i = 0; i < 256; i++)
            if (keysPressed.get(i)) pressed.add(i);
        return pressed;
    }

    public static boolean isHeld(int kc) {       // Returns key held value
        try { return keysHeld.get(kc);
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    public static boolean isReleased(int kc) {       // Returns key released value
        try { return keysReleased.get(kc);
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    public static boolean buttonPressed(int button) {
        return buttonsPressed.get(button) && !mouseOnUI;
    }

    public static boolean UIButtonPressed(int button) {
        return buttonsPressed.get(button);
    }

    public static boolean buttonHeld(int button) {
        return buttonsHeld.get(button) && !mouseOnUI;
    }

    public static boolean UIButtonHeld(int button) {
        return buttonsHeld.get(button);
    }

    public static boolean buttonReleased(int button) {
        return buttonsReleased.get(button) && !mouseOnUI;
    }

    public static boolean UIButtonReleased(int button) {
        return buttonsReleased.get(button);
    }

    public static ScreenPosition getMousePos() {
        return mousePos;
    }

    public static int getWheelRotation() {
        return wheelRotation;
    }

    public static boolean isMouseOnUI() {
        return mouseOnUI;
    }

    public static void setMouseOnUI(boolean mouseOnUI) {
        Listener.mouseOnUI = mouseOnUI;
    }

    // Extra handler events (to have multiple inheritances in Java, you must use implements with interfaces, and
    // you must implement every function of every interface, not just the ones you wan to use)

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
