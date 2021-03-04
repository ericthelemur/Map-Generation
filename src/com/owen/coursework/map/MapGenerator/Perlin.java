package com.owen.coursework.map.MapGenerator;

import java.util.Random;

public class Perlin {
    private final short[] perm;

    public Perlin(long seed) {
        Random rand = new Random(seed);

        // Shuffle array with 0-255, to use in pseudo-random gradient generation
        // Duplicated to simplify h__ creation
        perm = new short[512];
        for (short i = 0; i < 256; i++) perm[i] = i;

        for (int i = 255; i > 0; i--) { // Fisher-Yates shuffle
            int j = rand.nextInt(i+1);
            short v = perm[i];
            perm[256+i] = perm[i] = perm[j];
            perm[j] = v;
        }
    }

    public double eval(double x, double y) {
        int X = fastFloor(x) & 255, Y = fastFloor(y) & 255;   // top-left coords of containing unit square, limit to < 255 (length of permutations)

        int h00 = perm[perm[X] + Y],   h01 = perm[perm[X+1] + Y],       // pseudo random hash generation of the gradient of each point
            h10 = perm[perm[X] + Y+1], h11 = perm[perm[X+1] + Y+1];

        double dx = x - fastFloor(x), dy = y - fastFloor(y);    // Coords from top-left

        double n00 = grad(h00, dx, dy),        n01 = grad(h01, dx - 1, dy),
               n10 = grad(h10, dx, dy - 1), n11 = grad(h11, dx - 1, dy - 1);

        double u = quintic(dx), v = quintic(dy);
        return lerp2d(n00, n01, n10, n11, u, v);    // Take weighted average
    }

    private static final int NOHASHES = 16;
    private double grad(int hash, double x, double y) {     // Selects a gradient and takes it's dot product with the input coordinates
        double deg = 2*Math.PI * ((double) hash / NOHASHES);
        return x*Math.cos(deg) + y*Math.sin(deg);
    }

    private double lerp2d(double v00, double v01, double v10, double v11, double u, double v) {
        return lerp(lerp(v00, v01, u),                  // v00  v01
                    lerp(v10, v11, u), v);              // v10  v11
    }

    private double lerp(double v1, double v2, double w) {
        return v1*(1-w) + v2*w;
    }

    private double quintic(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // MUCH faster than built in, from OpenSimplexNoise
    private static int fastFloor(double v) {
        int V = (int) v;
        return v < V ? V - 1 : V;
    }
}
