package com.owen.coursework;

import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;

public class Quadtree<E extends Point2D> {
    private Rectangle2D.Double boundary;
    private int bucketCapactiy = 10;

    private ArrayList<E> points = new ArrayList<>();

    private Quadtree<E> TL;     // Top Left
    private Quadtree<E> TR;     // Top Right
    private Quadtree<E> BL;     // Bottom Left
    private Quadtree<E> BR;     // Bottom Right

    public Quadtree(Rectangle2D.Double boundary) {
        this.boundary = boundary;
    }

    public Quadtree(Rectangle2D.Double boundary, int bucketCapacity) {
        this.boundary = boundary;
        this.bucketCapactiy = bucketCapacity;
    }

    public boolean insert(E point) {
        if (!boundary.contains(point)) return false;

        if (points.size() < bucketCapactiy && TL == null) {    // If space to add and no children
            points.add(point);
            return true;
        }

        if (TL == null) subdivide();    // If not space to add, and no children, create children
        return (TL.insert(point) || TR.insert(point) || BL.insert(point) || BR.insert(point));  // Try to insert into each child (stops at first true)
    }

    public void subdivide() {   // Creates child quadtrees, one for each quarter
        double x = boundary.x, y = boundary.y, w = boundary.width/2, h = boundary.height/2;
        TL = new Quadtree<>(new Rectangle2D.Double(x, y, w, h), bucketCapactiy);
        TR = new Quadtree<>(new Rectangle2D.Double(x+w, y, w, h), bucketCapactiy);
        BL = new Quadtree<>(new Rectangle2D.Double(x, y+h, w, h), bucketCapactiy);
        BR = new Quadtree<>(new Rectangle2D.Double(x+w, y+h, w, h), bucketCapactiy);
        // Insert points into child trees, removing from self
        points.removeIf(point -> TL.insert(point) || TR.insert(point) || BL.insert(point) || BR.insert(point));
    }

    public ArrayList<E> queryRange(Rectangle2D.Double range) {
        ArrayList<E> pointsInRange = new ArrayList<>();

        if (!boundary.intersects(range)) return pointsInRange;  // If no intersection, end now

        for (E point : points) if (range.contains(point)) pointsInRange.add(point); // Add points within range

        if (TL == null) return pointsInRange;   // If no children end now

        pointsInRange.addAll(TL.queryRange(range));     // Check children for matches
        pointsInRange.addAll(TR.queryRange(range));
        pointsInRange.addAll(BL.queryRange(range));
        pointsInRange.addAll(BR.queryRange(range));
        return pointsInRange;
    }

    public ArrayList<E> queryCircle(E centre, double r) {
        points = queryRange(new Rectangle2D.Double(centre.getX()-r, centre.getY()-r, r*2, r*2));    // Get rectangle
        points.removeIf(point -> centre.distanceSq(point) > r*r);   // Filter ones outside circle
        return points;
    }

    public ArrayList<E> getAll() {
        if (TL == null) return points;
        ArrayList<E> result = new ArrayList<>();
        result.addAll(TL.getAll());
        result.addAll(TR.getAll());
        result.addAll(BL.getAll());
        result.addAll(BR.getAll());
        return result;
    }

    public int size() {
        if (TL == null) return points.size();
        return TL.size() + TR.size() +BL.size() + BR.size();
    }

    public boolean contains(WorldPosition wp) {
        return boundary.contains(wp);
    }

    public void draw(Graphics2D g) {
        g.draw(boundary);
        if (TL == null) return;
        TL.draw(g);
        TR.draw(g);
        BL.draw(g);
        BR.draw(g);
    }

    @Override
    public String toString() {
        return "Quadtree " + String.format("%.2f %.2f %.2f %.2f", boundary.x, boundary.y, boundary.width, boundary.height)
                + ", contains " + size() + " points=" + points
                + ((TL == null) ? "" : ("\n"
                        + ("TL: " + TL) + ("TR: " + TR)
                        + ("BL: " + BL) + ("BR: " + BR))
                    .replaceAll("\n", "\n    "))
                + "\n";
    }

    public static void main(String[] args) {
        Quadtree<WorldPosition> qt = new Quadtree<>(new Rectangle2D.Double(0, 0, 100, 100));
        for (int i = 0; i < 100; i += 10) for (int j = 0; j < 100; j += 10)
                qt.insert(new WorldPosition(i, j));

        System.out.println(qt);
        ArrayList<WorldPosition> query = qt.queryRange(new Rectangle2D.Double(25, 25, 50, 50));
        query.sort(((Comparator<WorldPosition>) (o1, o2) -> Double.compare(o1.x, o2.x)).thenComparing((o1, o2) -> Double.compare(o1.y, o2.y)));
        System.out.println(query);
        System.out.println(qt.size());
    }
}
