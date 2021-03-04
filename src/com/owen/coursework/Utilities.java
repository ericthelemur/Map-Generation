package com.owen.coursework;

import com.owen.coursework.map.Position.WorldPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class Utilities {
    public static void drawMultipleLines(String ss, int x, int y, Graphics g) {
        String[] strings = ss.split("\n");
        int stringHeight = g.getFontMetrics().getHeight();
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            g.drawString(s, x, y+(i+1)*stringHeight);
        }
    }

    public static void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        g.drawString(text, x - metrics.stringWidth(text)/2, y-metrics.getHeight()/2+metrics.getAscent());
    }

    public static void drawMultipleCentredLines(String text, int x, int y, Graphics g) {
        String[] strings = text.split("\n");
        FontMetrics metrics = g.getFontMetrics();
        y -= 0.5*metrics.getHeight()*strings.length + metrics.getAscent();

        for (String s : strings) {
            drawCenteredString(g, s, x - metrics.stringWidth(text) / 2, y += metrics.getHeight());
        }
    }

    public static void drawLine(double x1, double y1, double x2, double y2, Graphics2D g) {
        Line2D.Double l = new Line2D.Double(x1, y1, x2, y2);
        g.draw(l);
    }

    public static void drawLine(WorldPosition wp1, WorldPosition wp2, Graphics2D g) {
        Line2D.Double l = new Line2D.Double(wp1, wp2);
        g.draw(l);
    }

    public static void fillCircle(WorldPosition centre, double r, Graphics2D g) {
        g.fill(new Ellipse2D.Double(centre.x-r, centre.y-r, r*2, r*2));
    }

    public static void drawCircle(WorldPosition centre, double r, Graphics2D g) {
        g.draw(new Ellipse2D.Double(centre.x-r, centre.y-r, r*2, r*2));
    }

    public static Path2D.Double constructPath(double[] xpoints, double[] ypoints) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(xpoints[0], ypoints[0]);
        for (int i = 1; i < xpoints.length; i++) {
            p.lineTo(xpoints[i], ypoints[i]);
        }
        p.closePath();
        return p;
    }

    public static void fillPath(double[] xpoints, double[] ypoints, Graphics2D g) {
        g.fill(constructPath(xpoints, ypoints));
    }

    // https://stackoverflow.com/a/27461352
    public static void drawArrow(double x1, double y1, double x2, double y2, double d, double h, Graphics2D g) {
        double dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        double avgX = (x2-x1)/2, avgY = (y2-y1)/2;
        double[] xpoints = {x2-avgX, xm-avgX, xn-avgX};
        double[] ypoints = {y2-avgY, ym-avgY, yn-avgY};

        drawLine(x1, y1, x2, y2, g);
        fillPath(xpoints, ypoints, g);
    }

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static double clamp(double v, double min, double max) {
        if(v > min) {
            if(v < max) {
                return v;
            } else return max;
        } else return min;
    }

    public static double normalize(double n, double oldMin, double oldMax, double newMin, double newMax) {
        return ((n-oldMin)/(oldMax-oldMin)) * (newMax-newMin) + newMin;
    }

    public static double normalize(double n, double oldMin, double oldMax) {
        return (n-oldMin)/(oldMax-oldMin);
    }

    public static double lerp(double v1, double v2, double w) {
        return (v1*(1-w) + v2*w);
    }

    public static int lerp(int v1, int v2, double w) {
        return (int) (v1*(1-w) + v2*w);
    }

    public static Color valToColour(double v) {
        int c =  (int) (clamp(v, 0, 1)*255);
        return new Color(c, c, c);
    }

    public static Color valToColour(double v, double min, double max) {
        int c =  (int) (normalize(v, min, max, 0, 255));
        return new Color(c, c, c);
    }

    public static Color scaleColour(Color c, double factor) {
        factor = clamp(factor, 0, 1);
        return new Color((int) (c.getRed()*factor), (int) (c.getGreen()*factor), (int) (c.getBlue()*factor));
    }

    public static Color lerpColour(Color c1, Color c2, double w) {
        return new Color(lerp(c1.getRed(), c2.getRed(), w), lerp(c1.getGreen(), c2.getGreen(), w), lerp(c1.getBlue(), c2.getBlue(), w));
    }

    public static String capitalize(String string) {
        StringBuilder capitalized = new StringBuilder();
        capitalized.append(Character.toUpperCase(string.charAt(0)));
        for (int i = 1; i < string.length(); i++) {
            if (!Character.isLetter(string.charAt(i-1))) capitalized.append(Character.toUpperCase(string.charAt(i)));
            else capitalized.append(Character.toLowerCase(string.charAt(i)));
        }
        return capitalized.toString();
    }
}
