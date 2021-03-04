package com.owen.coursework;

import com.owen.coursework.map.Map;
import com.owen.coursework.map.Position.DoubleDimension;
import com.owen.coursework.map.Position.WorldPosition;

import java.awt.geom.Rectangle2D;


public class Camera {
    private static final boolean LIMIT_CAMERA = true;
    private WorldPosition offset;

    private double zoom, targetZoom;
    private boolean changed = false;

    private int border;
    public Rectangle2D.Double view;
    public double minZoom, maxZoom;
    private double panSpeed, zoomSpeed;

    // Constructor
    public Camera(int x, int y, double zoom, int border, double minZoom, double maxZoom, double panSpeed, double zoomSpeed) {
        this.offset = new WorldPosition(x, y);
        this.zoom = zoom;
        targetZoom = zoom;
        this.border = border;
        view = new Rectangle2D.Double(offset.x, offset.y, Game.screenSize.width / zoom, Game.screenSize.height / zoom);
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.panSpeed = panSpeed;
        this.zoomSpeed = zoomSpeed;
    }

    public void checkLimits() {
        // Limits zoom
        if (LIMIT_CAMERA && !Game.debug) targetZoom = Utilities.clamp(targetZoom, minZoom, maxZoom);
        zoom = Utilities.lerp(zoom, targetZoom, 0.3);   // Smooths transition to target zoom

        DoubleDimension oldView = new DoubleDimension(view.width, view.height);
        view.width = (int) ((double) Game.screenSize.width/zoom);           // corrects view size
        view.height = (int) ((double) Game.screenSize.height/zoom);

        // Limit view positions
        view.x += (int) (((double) Listener.getMousePos().x/Game.screenSize.width)*(oldView.width-view.width));
        view.y += (int) (((double) Listener.getMousePos().y/Game.screenSize.height)*(oldView.height-view.height));

        Rectangle2D.Double bounds = Map.bounds;     // If view completely covers the map, zoom with the map centred
        if (view.x < bounds.x && view.getMaxX() > bounds.getMaxX()) view.x = (bounds.x+bounds.width*0.5)-view.width/2;
        else if (LIMIT_CAMERA && !Game.debug) view.x = Utilities.clamp(view.x, bounds.x - border, bounds.getMaxX() - view.width + border);

        if (view.y < bounds.y && view.getMaxY() > bounds.getMaxY()) view.y = (bounds.y+bounds.height*0.5)-view.height/2;
        else if (LIMIT_CAMERA && !Game.debug) view.y = Utilities.clamp(view.y, bounds.y - border, bounds.getMaxY() - view.height + border);
    }

    public void setOffset(int x, int y) {
        view.x = x;
        view.y = y;
        changed = true;
    }

    public void addOffset(int x, int y) {
        view.x += x;
        view.y += y;
        changed = true;
    }

    public void pan(int xAmt, int yAmt) {
        view.x += panSpeed*xAmt*zoom;
        view.y += panSpeed*yAmt*zoom;
        changed = true;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        changed = true;
    }

    public void incZoom(double factor) {
        targetZoom *= factor;
        changed = true;
    }

    public void decZoom(double factor) {
        targetZoom /= factor;
        changed = true;
    }

    public void zoom(double zoomAmt) {
        targetZoom *= Math.exp(zoomAmt*zoomSpeed);
        changed = true;
    }

    public double getTargetZoom() {
        return targetZoom;
    }

    public void setTargetZoom(double targetZoom) {
        this.targetZoom = targetZoom;
    }

    public boolean hasChanged() {
        return changed;
    }

    public Rectangle2D.Double getView() {
        return view;
    }
}
