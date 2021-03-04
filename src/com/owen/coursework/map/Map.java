package com.owen.coursework.map;

import com.owen.coursework.Camera;
import com.owen.coursework.Game;
import com.owen.coursework.Listener;
import com.owen.coursework.Utilities;
import com.owen.coursework.map.Entities.MapEntities.Nation;
import com.owen.coursework.map.Entities.MapEntities.Town;
import com.owen.coursework.map.Entities.MapEntities.Tree;
import com.owen.coursework.Quadtree;
import com.owen.coursework.map.Entities.Entity;
import com.owen.coursework.map.Entities.MapEntities.Capital;
import com.owen.coursework.map.Entities.MapEntities.MapLabel;
import com.owen.coursework.map.Position.MapPosition;
import com.owen.coursework.map.Position.ScreenPosition;
import com.owen.coursework.map.Position.WorldPosition;
import com.owen.coursework.states.LoadState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Map {
    private static int RIVERTHRESHOLD;

    private static boolean updateMap = true;
    private static BufferedImage image, smallImage;

    public static Color backgroundColour = Biome.Ocean.colour;
    public static Dimension size;
    public static Rectangle2D.Double bounds;

    private static Hex[][] hexes;
    private static Quadtree<Hex> hexQuadtree;
    // Objects on map
    public static PriorityQueue<MapLabel> mapLabels = new PriorityQueue<>(20, (l1, l2) -> l2.priority - l1.priority);

    public static ArrayList<Landmass> landmasses = new ArrayList<>();
    public static ArrayList<Waterbody> waterbodies = new ArrayList<>();
    public static ArrayList<Town> cities = new ArrayList<>();
    public static ArrayList<Nation> nations = new ArrayList<>();
    private static ArrayList<RoadRoute> roadRoutes = new ArrayList<>();
    public static ArrayList<Tree> trees = new ArrayList<>();
    private static ArrayList<Entity> entities = new ArrayList<>();
    public static Hex selected; // For road debug image

    public static double seaLevel = 0, maxRiverSize;

    private final static Logger logger = Logger.getLogger(Game.class.getName());

    public enum dispModes { // Used for debug values display
        BIOME, TERRITORY, CITYCHANCE, ELEVATION, WATERELEVATION, WATERFLOW, GRADIENT, RAINFALL, LANDMASS, ROADMAP;

        public dispModes next() {
            updateMap();
            return values()[Utilities.mod((ordinal() + 1), values().length)];
        }

        public dispModes prev() {
            updateMap();
            return values()[Utilities.mod((ordinal() - 1), values().length)];
        }
    }
    public static dispModes dispMode = dispModes.BIOME;

    public static void createMap(int width, int height) {
        size = new Dimension(width, height);        // Set bounds
        RIVERTHRESHOLD = width * height / 800;
        bounds = new Rectangle2D.Double(0, 0, (size.getWidth() + 0.5) * Game.hexSize.getWidth(),
                                                     ((size.getHeight()) * 0.75 + 0.25) * Game.hexSize.getHeight());
        logger.info("Map Size: " + size + "  " + bounds);

        hexes = new Hex[width][height];
        hexQuadtree = new Quadtree<>(new Rectangle2D.Double(0, 0, width, height));
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            Hex hex = new Hex(x, y);
            hexes[x][y] = hex;
            hexQuadtree.insert(hex);        // Create hex objects
        }
        // Set zoom limit to just fit map
        Game.States.GameState.camera.minZoom = Math.min(Game.screenSize.width / bounds.width,
                                                        Game.screenSize.height / bounds.height);

        mapLabels = new PriorityQueue<>(20, (l1, l2) -> l2.priority - l1.priority);
        landmasses = new ArrayList<>();
        waterbodies = new ArrayList<>();
        cities = new ArrayList<>();
        nations = new ArrayList<>();
        roadRoutes = new ArrayList<>();
        trees = new ArrayList<>();
        entities = new ArrayList<>();
    }

    public static void draw(Graphics2D g) {
        Camera camera = Game.States.GameState.camera;
        AffineTransform old = g.getTransform();

        g.scale(camera.getZoom(), camera.getZoom());            // Translate screen from camera
        g.translate(-camera.getView().x, -camera.getView().y);

        if (updateMap || image == null) generateMapImage();     // Redraw map if required

        if (camera.getZoom() < 0.25) g.drawImage(smallImage, (int) -Game.hexSize.width, (int) -Game.hexSize.height, smallImage.getWidth()*4, smallImage.getHeight()*4, null);
        if (camera.getZoom() <= 1)       // If zoomed out draw generated image
            g.drawImage(image, (int) -Game.hexSize.width, (int) -Game.hexSize.height, null);
        else {      // If zoomed in enough, draw relevant area every time for more precision
            // Calculate visible hexes
            MapPosition mp1 = new WorldPosition(camera.getView().x, camera.getView().y).toMapPosition();
            mp1 = (mp1 != null ? mp1 : new MapPosition(0, 0)).add(-2, -2);
            MapPosition mp2 = new WorldPosition(camera.getView().x + camera.getView().width, camera.getView().y + camera.getView().height).toMapPosition();
            mp2 = (mp2 != null ? mp2 : new MapPosition(size.width, size.height)).add(2, 2);

            drawMap(mp1, mp2, g);
        }

        if (dispMode == dispModes.ROADMAP) {    // Draw road debug
            drawDebugRoads(g);
            drawTerritory(g);
        }

        for (Entity e : entities) e.draw(g);    // Draw features
        for (Town town : cities) town.draw(g);

        g.setColor(Color.BLACK);
        MapPosition mp = Listener.getMousePos().toMapPosition();
        if (mp != null && !Listener.isMouseOnUI()) hexes[mp.x][mp.y].draw(g);   // Draw selected hex

        g.setTransform(old);    // Undo transformation

        drawLabels(g);  // Draw map labels in correct positions
    }

    private static void generateMapImage() {
        Game.setState(Game.States.LoadState);
        Game.updateLoadingMessage("Generating Map Image");
        logger.warning("Generating new map image " + dispMode);
        image = null;
        smallImage = null;
        System.gc();    // Force garbage collection to clear old image
        image = new BufferedImage((int) (bounds.width + Math.ceil(Game.hexSize.width)),
                (int) (bounds.height + (int) Math.ceil(Game.hexSize.height)), BufferedImage.TYPE_INT_ARGB);


        smallImage = new BufferedImage((int) (bounds.width + Math.ceil(Game.hexSize.width))/4,
                (int) (bounds.height + (int) Math.ceil(Game.hexSize.height))/4, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        // Set rendering hints (antialiasing, etc.)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.translate(Game.hexSize.width, Game.hexSize.height);

        drawMap(new MapPosition(0, 0), new MapPosition(size.width, size.height), g);
        g.dispose();                        // Draw map
        g = smallImage.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth()/4, image.getHeight()/4, null);
        updateMap = false;
        Game.finishedLoading();
    }

    private static void drawMap(MapPosition topLeft, MapPosition bottomRight, Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(1));

        // Calculate scale factor for gradient and city chance, since they can be too small or too large to scale easily
        double min = 1, max = 0;
        if (dispMode == dispModes.GRADIENT || dispMode == dispModes.CITYCHANCE) {
            for (int x2 = 0; x2 < size.width; x2++) for (int y2 = 0; y2 < size.height; y2++) {
                    double v;
                    if (dispMode == dispModes.GRADIENT) v = getHex(x2, y2).gradient;
                    else if (dispMode == dispModes.CITYCHANCE) v = getHex(x2, y2).cityChance;
                    else continue;
                    if (v > max) max = v;
                    else if (v < min) min = v;
                }
        }

        // For each hex
        for (int x = Math.max(0, topLeft.x); x < Math.min(size.getWidth(), bottomRight.x); x++) {
            for (int y = Math.max(0, topLeft.y); y < Math.min(size.getHeight(), bottomRight.y); y++) {
                if (hexes[x][y] == null) {
                    logger.warning("Hex " + x + " " + y + " is null");
                    continue;
                }
                switch (dispMode) {
                    case BIOME:
                    case TERRITORY:
                        g.setColor(hexes[x][y].biome.colour);   // Draw standard biome colours
                        break;
                    case ROADMAP:
                    case ELEVATION:
                        g.setColor(hexes[x][y].biome.getType() == BiomeTag.Ocean ?
                                Utilities.scaleColour(Biome.Ocean.colour, hexes[x][y].elevation) :
                                Utilities.valToColour(hexes[x][y].elevation));
                        break;
                    case WATERELEVATION:
                    case WATERFLOW:
                        g.setColor(Utilities.scaleColour(Biome.Ocean.colour, hexes[x][y].getWaterElevation()));
                        break;
                    case GRADIENT:
                        g.setColor(Utilities.valToColour(hexes[x][y].gradient, min, max));
                        break;
                    case CITYCHANCE:
                        g.setColor(Utilities.valToColour(hexes[x][y].cityChance, min, max));
                        break;
                    case RAINFALL:
                        g.setColor(Utilities.scaleColour(Color.WHITE, hexes[x][y].rainfall));
                        break;
                    case LANDMASS:     // Calculate colour of landmass/waterbody to scale from 0-255 from length of array
                        Hex hex = getHex(x, y);
                        if (hex.area == null) continue;
                        if (hex.biome.getType() == BiomeTag.Ocean) {
                            double m = 1 - Utilities.clamp((double) hex.area.ID / waterbodies.size(), 0, 1);
                            if (Double.isInfinite(m)) continue;

                            g.setColor(new Color((int) (136 * m), (int) (136 * m), (int) (244 * m)));
                        } else if (hex.biome.getType() == BiomeTag.Land) {
                            double m = 1 - (double) hex.area.ID / landmasses.size();
                            g.setColor(new Color((int) Utilities.clamp(196 * m, 0, 255),
                                    (int) Utilities.clamp(212 * m, 0, 255),
                                    (int) Utilities.clamp(170 * m, 0, 255)));
                        }
                        break;
                }
                hexes[x][y].fill(g);        // Fill hex
                g.setColor(Color.black);
            }
        }

        if (dispMode == dispModes.WATERFLOW) {      // Draw arrows on top if drawing flow directions
            for (int x = 0; x < size.getWidth(); x++)
                for (int y = 0; y < size.getHeight(); y++) {
                    if (hexes[x][y].flow == null) continue;
                    WorldPosition wp1 = hexes[x][y].toWorldPosition(), wp2 = hexes[x][y].flow.toWorldPosition();
                    g.setStroke(new BasicStroke((float) (Math.sqrt(hexes[x][y].waterFlux)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Utilities.drawArrow(wp1.x, wp1.y, wp2.x, wp2.y, 20, 10, g);
                }
        }

        if (dispMode == dispModes.BIOME) {      // If viewing biomes draw extra features
            drawTrees(g);
            drawRivers(g, null);
            drawRoads(g, null);
            drawTerritory(g);
        }

        g.setColor(Color.BLACK);
        g.setStroke(oldStroke);
    }

    private static void drawRivers(Graphics2D g, Rectangle area) {
        g.setColor(Biome.Lake.colour);
        for (int x = 0; x < size.getWidth(); x++) for (int y = 0; y < size.getHeight(); y++) {
            if (area != null && !area.contains(x, y)) continue;     // For each river hex
            Hex hex = hexes[x][y], flow = hex.flow;

            if (flow != null && hex.isRiver() && hex.biome.getType() != BiomeTag.Ocean) {
                WorldPosition wp = hex.toWorldPosition(), fwp = flow.toWorldPosition();
                                                                     // Draw curve from edge of hex towards each source
                for (Hex source : hex.sources) {                     // to flow edge, using hex centre as control point
                    if (!source.isRiver()) continue;
                    WorldPosition swp = source.toWorldPosition();
                    drawRiverSegment(swp, wp, fwp, source.waterFlux, g);
                }
            }
        }
    }

    private static void drawRiverSegment(WorldPosition swp, WorldPosition wp, WorldPosition fwp, double flux, Graphics2D g) {
        float width = (float) (Game.hexSize.width*0.75*Math.sqrt(flux/maxRiverSize));       // Calculates width
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Draw curve from hex-source edge to hex-flow edge
        g.draw(new QuadCurve2D.Double((swp.x+wp.x)/2, (swp.y+wp.y)/2,
                                            wp.x, wp.y,
                                            (fwp.x+wp.x)/2, (fwp.y+wp.y)/2));
    }


    private static final Color roadColour = new Color(121, 117, 106);
    public static void drawRoads(Graphics2D g, Rectangle area) {
        g.setColor(roadColour);

        HashSet<RoadConn> drawn = new HashSet<>();  // Stores drawn hexes to ensure roads are not redrawn
        for (RoadRoute rr : roadRoutes) {       // For each route
            int l = rr.path.size() - 1;
            for (int i = 0; i < rr.path.size(); i++) {      // Draw each section of road
                Hex src = rr.path.get(i == 0 ? 0 : (i - 1)), hex = rr.path.get(i), flo = rr.path.get(i == l ? l : (i + 1));

                if (area != null && !area.contains(hex)) continue;  // If onscreen
                RoadConn rc = new RoadConn(src, hex, flo);
                if (drawn.contains(rc)) continue;       // Checks road has not already been drawn
                drawn.add(rc);

                double size = getSize(src, hex, flo);
                float width = (float) Math.min(Game.hexSize.width, Math.sqrt(size / 1000));
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawRoadSegment(src, hex, flo, g);
            }
        }
    }

    private static class RoadConn {
        Hex src, hex, flo;

        public RoadConn(Hex src, Hex hex, Hex flo) {
            this.src = src;
            this.hex = hex;
            this.flo = flo;
        }

        @Override
        public boolean equals(Object o) {           // Default equivalence check for comparing contained variables
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoadConn roadConn = (RoadConn) o;
            return Objects.equals(src, roadConn.src) && Objects.equals(hex, roadConn.hex) &&
                    Objects.equals(flo, roadConn.flo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, hex, flo);
        }
    }

    private static void drawRoadSegment(Hex src, Hex hex, Hex flo, Graphics2D g) {
        WorldPosition swp = src.toWorldPosition(), wp = hex.toWorldPosition(), fwp = flo.toWorldPosition();
        g.draw(new QuadCurve2D.Double((swp.x+wp.x)/2, (swp.y+wp.y)/2,
                                        wp.x, wp.y,
                                    (fwp.x+wp.x)/2, (fwp.y+wp.y)/2));
    }

    private static double getSize(Hex source, Hex hex, Hex target) { // Gets size as the smallest of the 3 sizes, so smoother transitions
        return Math.min(source == hex ? Double.MAX_VALUE : Math.min(getSize_I(hex, source), getSize_I(source, hex)),
                        target == hex ? Double.MAX_VALUE : Math.min(getSize_I(hex, target), getSize_I(target, hex)));
    }

    private static double getSize_I(Hex hex, Hex target) {
        for (Hex.Road road : hex.roads) if (road.to == target) return road.size;
        return 0;
    }



    private static void drawDebugRoads(Graphics2D g) {       // Is Debug
        Town selectedTown = selected == null ? null : selected.town;
        Town hovered = null;
        int routes = 0;
        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        if (Listener.getMousePos().toMapPosition() != null) hovered = Map.getHex(Listener.getMousePos().toMapPosition()).town;

        if (hovered != null && selectedTown == null)
            for (RoadRoute rr : roadRoutes)
                if (rr.town1 == hovered || rr.town2 == hovered) routes++;


        g.setColor(roadColour);
        for (int j = 0; j < roadRoutes.size(); j++) {
            RoadRoute rr = roadRoutes.get(j);
            if (selectedTown != null && hovered != null && selectedTown != hovered) {
                if (!((rr.town1 == hovered && rr.town2 == selectedTown) || (rr.town1 == selectedTown && rr.town2 == hovered))) continue;
                g.setColor(Color.RED);
            } else if (hovered != null) {
                if (rr.town1 != hovered && rr.town2 != hovered) continue;
                g.setColor(Color.getHSBColor((float) j / routes, 1, 1));
            } else g.setColor(Color.getHSBColor((float) j / roadRoutes.size(), 1, 1));


            int l = rr.path.size() - 1;
            for (int i = 0; i < rr.path.size(); i++) {
                Hex src = rr.path.get(i == 0 ? 0 : (i - 1)), hex = rr.path.get(i), flo = rr.path.get(i == l ? l : (i + 1));

                double size = getSize(src, hex, flo);
                float width = (float) Math.min(Game.hexSize.width, Math.sqrt(size / 2000));
                if (dispMode == dispModes.ROADMAP && hovered != null) {
                    width *= 4;
                    if (selectedTown != null) width = Game.hexSide*0.75f;
                }
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawRoadSegment(src, hex, flo, g);
            }
        }

        g.setComposite(oldComposite);
    }

    public static void drawTrees(Graphics2D g) {
        g.setStroke(new BasicStroke(2));
        for (Tree tree : trees) tree.draw(g);
    }

    public static void drawTerritory(Graphics2D g) {
        Composite oldComposite = g.getComposite();      // Draws with transparency
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setStroke(new BasicStroke(1.25f*Game.hexSide, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Nation nation : nations) {     // Draws each nation's border
            g.setColor(nation.colour);
            g.draw(nation.borderShape);
        }
        g.setComposite(oldComposite);
    }

    public static void drawLabels(Graphics2D g) {
        for (MapLabel label : mapLabels) {
            ScreenPosition sp = label.getScreenPosition();
            if (labelIsIntersecting(label, g)) continue;        // Check label doesn't overlap
            if (g.getFont() != label.getFont()) g.setFont(label.getFont());
            Utilities.drawCenteredString(g, label.text, sp.x, sp.y);
        }
    }

    private static boolean labelIsIntersecting(MapLabel mapLabel, Graphics2D g) {
        Rectangle rect1 = mapLabel.getBoundingRect(g);
        for (MapLabel label2: mapLabels) {
            if (mapLabel == label2) break;
            Rectangle rect2 = label2.getBoundingRect(g);
            if (rect1.intersects(rect2) && !labelIsIntersecting(label2, g)) return true;
        }
        return false;
    }

    public static void saveMap() {
        Game.setState(new LoadState());
        Game.updateLoadingMessage("Saving...");
        String saveDate = new SimpleDateFormat("yy-MM-dd HH-mm-ss").format(new Date());
        try {       // Create image, graphics, and set rendering hints
            BufferedImage map = new BufferedImage(Map.image.getWidth(), Map.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = map.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.drawImage(Map.image, 0, 0, null);
            drawImg(g, 1);         // Draw map image, then draw other features
            ImageIO.write(map, "png", new File(saveDate + "-Island.png"));
            g.dispose();

            map = new BufferedImage(Map.smallImage.getWidth(), Map.smallImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            g = map.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.drawImage(Map.smallImage, 0, 0, null);
            drawImg(g, 0.25);         // Draw map image, then draw other features
            ImageIO.write(map, "png", new File(saveDate + "-SmallIsland.png"));
            g.dispose();
        } catch (IOException e) {           // Save
            e.printStackTrace();
        }
        Game.finishedLoading();
    }

    private static void drawImg(Graphics2D g, double zoom) {
        g.scale(zoom, zoom);
        g.translate(Game.hexSize.width, Game.hexSize.height);
        for (Entity e : entities) e.draw(g);        // Draws features
        for (Town town : cities) town.draw(g);
        g.translate(-Game.hexSize.width, -Game.hexSize.height);
        g.scale(1/zoom, 1/zoom);

        g.translate(Game.hexSize.width, Game.hexSize.height);

        g.setColor(Color.BLACK);
        double sf =  2*Map.bounds.width / Game.screenSize.width;      // Scale factor for label size
        for (MapLabel label : mapLabels) {                  // Custom calculation of label size for scale
            WorldPosition wp = WorldPosition.add(label.worldPosition, label.offset.x*sf*zoom*zoom, label.offset.y*sf*zoom*zoom);
            Font font = new Font(label.getFont().getName(), Font.PLAIN, (int) (label.getFont().getSize()*sf*zoom));
            g.setFont(font);                // Enlarge font
            Utilities.drawCenteredString(g, label.text, (int) wp.x, (int) wp.y);    // Draw string
        }
        g.translate(-Game.hexSize.width, -Game.hexSize.height);
    }
    
    public static ArrayList<Hex> queryRange(Rectangle2D.Double range) {
        return hexQuadtree.queryRange(range);
    }

    public static Hex getHex(MapPosition pos) {
        return getHex(pos.x, pos.y);
    }

    public static Hex getHex(int x, int y) {
        return hexes[x][y];
    }

    public static void addEntity(Entity e) {
        entities.add(e);
    }

    public static void addNation(Nation nation) {
        nations.add(nation);
        if (!cities.contains(nation.capital)) cities.add(nation.capital);
    }

    public static void addRoadRoute(RoadRoute roadRoute) {
        roadRoutes.add(roadRoute);
        ArrayList<Hex> newP = new ArrayList<>(roadRoute.path);
        Collections.reverse(newP);
        roadRoutes.add(new RoadRoute(roadRoute.town2, roadRoute.town1, newP));

        for (int i = 1; i < roadRoute.path.size(); i++) {
            roadRoute.path.get(i).addRoad(new Hex.Road(roadRoute.path.get(i - 1),
                    roadRoute.town1.size + roadRoute.town2.size), roadRoute.town1, roadRoute.town2);
            roadRoute.path.get(i-1).addRoad(new Hex.Road(roadRoute.path.get(i),
                    roadRoute.town1.size + roadRoute.town2.size), roadRoute.town1, roadRoute.town2);
        }
    }

    public static RoadRoute roadTo(Hex hex, Town town2) {
        for (RoadRoute roadRoute : roadRoutes)
            if (roadRoute.town2 == town2)
                if (roadRoute.path.contains(hex)) return roadRoute;
        return null;
    }

    public static void setDispMode(dispModes dispMode) {
        if (dispMode == Map.dispMode) return;
        Map.dispMode = dispMode;
        updateMap();
    }

    public static void updateMap() {
        updateMap = true;
    }

    public static void updateMapNow() {
        updateMap = false;
        generateMapImage();
    }



    // Map auxiliary data types

    public enum BiomeTag {
        Ocean, Land, Forest
    }

    public enum Biome {     // Biome definitions
        Blank("Blank",                                          new Color(255, 0,   255), 1, BiomeTag.Land),
        Ocean("Ocean",                                          new Color( 68, 68,  122), 0, BiomeTag.Ocean),
        Lake("Lake",                                            new Color(90,  90,  164), 0, BiomeTag.Ocean),
        Sea("Sea",                                              new Color( 80, 80,  141), 0, BiomeTag.Ocean),
        Beach("Beach",                                          new Color(233, 221, 199), 6, BiomeTag.Land),
        Scorched("Scorched",                                    new Color(153, 153, 153), 17, BiomeTag.Land),
        Bare("Bare",                                            new Color(187, 187, 187), 15, BiomeTag.Land),
        Tundra("Tundra",                                        new Color(211, 211, 187), 5, BiomeTag.Land),
        Snow("Snow",                                            new Color(248, 248, 248), 20, BiomeTag.Land),
        TemperateDesert("Temperate Desert",                     new Color(228, 232, 202), 4, BiomeTag.Land),
        Shrubland("Shrubland",                                  new Color(196, 204, 187), 3, BiomeTag.Land),
        Grassland("Grassland",                                  new Color(196, 212, 170), 2, BiomeTag.Land),
        TemperateDeciduousForest("Temperate Deciduous Forest",  new Color(180, 201, 169), 4, BiomeTag.Land, BiomeTag.Forest),
        TemperateRainforest("Temperate Rainforest",             new Color(164, 196, 168), 5, BiomeTag.Land, BiomeTag.Forest),
        SubtropicalDesert("Subtropical Desert",                 new Color(233, 221, 199), 3, BiomeTag.Land),
        TropicalSeasonalForest("Tropical Seasonal Forest",      new Color(169, 204, 164), 5, BiomeTag.Land, BiomeTag.Forest),
        TropicalRainforest("Tropical Rainforest",               new Color(156, 187, 169), 5, BiomeTag.Land, BiomeTag.Forest),
        ;

        public String name;
        public Color colour;
        public int cost;
        public ArrayList<BiomeTag> tags;

        Biome(String name, Color colour, int cost, BiomeTag... tags) {
            this.name = name;
            this.colour = colour;
            this.cost = cost;
            this.tags = new ArrayList<>(Arrays.asList(tags));
        }

        public boolean containsTag(BiomeTag tag) {
            return tags.contains(tag);
        }

        public BiomeTag getType() {
            return tags.contains(BiomeTag.Ocean) ? BiomeTag.Ocean : BiomeTag.Land;
        }

        @Override
        public String toString() {
            return "Biome{" +
                    "name='" + name + '\'' +
                    ", tags=" + tags +
                    '}';
        }
    }



    public static class Area {
        int ID, count = 0;
        ArrayList<Hex> members = new ArrayList<>();

        public Area(int ID) {
            this.ID = ID;
        }

        public void addHex(Hex hex) {
            members.add(hex);
            count++;
        }

        @Override
        public String toString() {
            return "Area{" + "ID=" + ID + ", count=" + count + '}';
        }
    }

    public static class Landmass extends Area {
        public Landmass(int ID) {
            super(ID);
        }

        @Override
        public String toString() {
            return "Landmass{" + "ID=" + ID + ", count=" + count + '}';
        }
    }

    public static class Waterbody extends Area {
        public Waterbody(int ID) {
            super(ID);
        }

        @Override
        public String toString() {
            return "Waterbody{" + "ID=" + ID + ", count=" + count + '}';
        }
    }



    public static class RoadRoute {     // Stores routes between cities
        public Town town1, town2;
        double score = 0;
        public ArrayList<Hex> path;

        public RoadRoute(Town town1, Town town2) {
            this.town1 = town1;
            this.town2 = town2;         // Calculates score
            score = town1.size * town2.size/ town1.distanceSq(town2);
            if (town1 instanceof Capital) score += 3000;
            if (town2 instanceof Capital) score += 3000;
            if (town1 instanceof Capital && town2 instanceof Capital) score += 6000;
        }
        
        public RoadRoute(Town town1, Town town2, ArrayList<Hex> path) {
            this.town1 = town1;
            this.town2 = town2;
            this.path = path;
        }

        public double getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoadRoute that = (RoadRoute) o;
            return score == that.score &&
                    (Objects.equals(town1, that.town1) && Objects.equals(town2, that.town2)) ||
                    (Objects.equals(town1, that.town2) && Objects.equals(town2, that.town1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(town1, town2, score);
        }

        @Override
        public String toString() {
            return "RoadRoute " + town1 +
                    " to " + town2 +
                    ", score=" + score;
        }
    }
}