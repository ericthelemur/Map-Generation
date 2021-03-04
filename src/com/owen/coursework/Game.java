package com.owen.coursework;


import com.owen.coursework.map.Map;
import com.owen.coursework.map.MapGenerator.MapGenerator;
import com.owen.coursework.map.Position.DoubleDimension;
import com.owen.coursework.states.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Game {
    public static long seed;
    public static Random rand;
    private static final Dimension defaultScreenSize = new Dimension(1000, 800);
    public static Dimension screenSize = new Dimension(defaultScreenSize);

    private static boolean running = false, fullscreen = true;
    public static boolean debug = false;

    private final static Logger logger = Logger.getLogger(Game.class.getName());

    private static final int targetFPS = 30;
    private static final long targetSPF = (long) (1e9 / targetFPS);
    private static int FPS = 0;
    private static JFrame frame;
    private static Insets insets;

    static {
            JFrame temp = new JFrame();
            temp.pack();
            insets = temp.getInsets();
        }

    private static Canvas canvas;
    private static BufferStrategy strategy;


    public static final int hexSide = 16;
    public static final DoubleDimension hexSize = new DoubleDimension(hexSide * Math.sqrt(3), hexSide * 2);

    private static Stack<BlankState> states = new Stack<>();

    public Game() {
        initialize();
        run();
    }

    private static void initialize() {  // Set up canvas
        initLogger();

        canvas = new Canvas();
        canvas.setBounds(0, 0, screenSize.width, screenSize.height);
        canvas.setIgnoreRepaint(true);
        canvas.setFocusTraversalKeysEnabled(false);

        Listener listener = new Listener();     // Add event handlers
        canvas.addKeyListener(listener);
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.addMouseWheelListener(listener);

        canvas.setFont(Fonts.smallFont);
        frame = new JFrame();
        frame.setName("HexTest");
        frame.add(canvas);

        updateGraphics();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(listener);

        canvas.requestFocus();
        frame.setVisible(true);

        canvas.createBufferStrategy(2);
        strategy = canvas.getBufferStrategy();

        setState(States.MenuState);
    }

    // http://hanoo.org/index.php?article=how-to-generate-logs-in-java
    private static void initLogger() {
        FileHandler fh;
        try {
            fh = new FileHandler("log.log", false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            return;
        }
        Logger l = Logger.getLogger("");
        fh.setFormatter(new SimpleFormatter());
        l.addHandler(fh);
        l.setLevel(Level.CONFIG);
    }

    public static void updateGraphics() {   // Changes from windowed to fullscreen and back
        frame.dispose();

        if (fullscreen) {
            frame.setUndecorated(true);
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setPreferredSize(screenSize);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            logger.info("Switched to fullscreen "+screenSize);
        } else {
            screenSize = new Dimension(defaultScreenSize);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setPreferredSize(new Dimension(defaultScreenSize.width + insets.left + insets.right, defaultScreenSize.height + insets.bottom + insets.top));
            frame.setUndecorated(false);
            frame.setResizable(false);
            logger.info("Switched to windowed "+screenSize);
        }

        canvas.setBounds(0, 0, screenSize.width, screenSize.height);

        frame.pack();
        frame.setVisible(true);

        frame.toFront();
        frame.requestFocus();
        canvas.requestFocus();
    }

    public static void generateMap(long seed) {
        Game.seed = seed;
        rand = new Random(seed);

        setState(States.LoadState);
        updateLoadingMessage("Creating Map...");
        Map.createMap(200, 200);
        MapGenerator mapGenerator = new MapGenerator(Map.size, rand.nextLong());
        mapGenerator.generate();
        updateLoadingMessage("Generating Map Image");
        Map.updateMapNow();
        updateLoadingMessage("Done!");
        finishedLoading();
    }

    private static void run() {
        running = true;
        long beginLoopTime=0, endLoopTime, deltaLoop = targetSPF, previousBeginTime;    // Calculates wait time
        double millisPassed;

        while (running) {   // Main loop
            millisPassed = deltaLoop/1e6;
            previousBeginTime = beginLoopTime;
            beginLoopTime = System.nanoTime();
            FPS = (int) (1e9 / (beginLoopTime-previousBeginTime));

            if (Listener.isPressed(KeyEvent.VK_ESCAPE)) {   // Pops up state stack on ESC press
                Game.previousState();
                if (states.size() <= 0) {
                    Game.stop();
                    break;
                }
            }

            manageControls(millisPassed);   // Calls state's relative functions

            update(millisPassed);

            draw(millisPassed);

            Listener.setMouseOnUI(false);

            endLoopTime = System.nanoTime();
            deltaLoop = endLoopTime - beginLoopTime;    // Calculate wait time for frame

            if (deltaLoop <= targetSPF) {
                try{
                    Thread.sleep((long) ((targetSPF - deltaLoop)/(1e6)));   // Wait for time
                }catch(InterruptedException ignored){}
            }
        }
        System.exit(0);
    }

    private static void manageControls(double millisPassed) {   // Calls state controls
        if (Listener.isPressed(KeyEvent.VK_F)) Game.toggleFullscreen();
        if (Listener.isPressed(KeyEvent.VK_B)) Game.toggleDebug();

        getState().manageControls(millisPassed);
    }

    private static void update(double millisPassed) {  // Calls state update
        getState().update(millisPassed);
        Listener.reset();
    }

    private static void draw(double millisPassed) {  // Calls state draw
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        getState().draw(g);
        if (debug) Utilities.drawMultipleLines(String.format("FPS: %d MSPF: %.3f State: %s\n", FPS, millisPassed, getState()), 10, 10, g);

        canvas.getBufferStrategy().show();
        g.dispose();
    }

    public static void stop() {
        running = false;
    }   // Ends program


    public static void setScreenSize(Dimension screenSize) {
        Game.screenSize = screenSize;
        updateGraphics();
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void setFullscreen(boolean fullscreen) {
        Game.fullscreen = fullscreen;
        updateGraphics();
    }

    public static void toggleFullscreen() {
        fullscreen = !fullscreen;
        updateGraphics();
    }

    public static void toggleDebug() {
        debug = !debug;
        logger.info("Debug toggled "+debug);
    }

    public static BlankState getState() {
        return states.peek();
    }

    public static void setState(BlankState state) {
        Game.states.push(state);
        logger.info("Switched to "+state);
    }

    public static BlankState previousState() {
        return Game.states.pop();
    }

    public static boolean updateLoadingMessage(String message) {
        if (getState() instanceof LoadState) {
            ((LoadState) getState()).setLoadMessage(message);
            return true;
        } else return false;
    }

    public static boolean finishedLoading() {
        if (getState() instanceof LoadState) {
            ((LoadState) getState()).finishedLoading();
            return true;
        } else return false;
    }

    public static void show() {
        strategy.show();
    }

    public static Graphics2D getGraphics() {
        return (Graphics2D) strategy.getDrawGraphics();
    }

    public static class States {
        public final static GameState GameState = new GameState();
        public final static com.owen.coursework.states.MenuState MenuState = new MenuState();
        public final static LoadState LoadState = new LoadState();
    }

    public static class Fonts {
        public final static Font XSFont = new Font("Segoe UI Semibold",Font.PLAIN, 11),
                           smallFont = new Font("Segoe UI",Font.BOLD, 16),
                           mediumFont = new Font("Segoe UI Semibold", Font.PLAIN, 24),
                           largeFont = new Font("Segoe UI Semibold", Font.PLAIN, 32),
                           XLFont = new Font("Segoe UI Semibold", Font.PLAIN, 60);
    }
}
