package com.owen.coursework.map.MapGenerator;

import com.owen.coursework.Utilities;

import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class Noise {
    private final int OCTAVES, SCALE, ZOOM;
    private final double LACUNARITY;
    private final double GAIN;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private final Rectangle2D.Double size;
    private final NoiseTypes noiseType;

    private Random rand;
    private Perlin[] octaves;

    public Noise(Rectangle2D.Double size, NoiseTypes noiseType, long seed, int OCTAVES, int SCALE, int ZOOM, double LACUNARITY, double GAIN) {
        this.size = size;
        this.OCTAVES = OCTAVES;
        this.SCALE = SCALE;
        this.ZOOM = ZOOM;
        this.LACUNARITY = LACUNARITY;
        this.GAIN = GAIN;
        this.noiseType = noiseType;

        rand = new Random(seed);
        octaves = new Perlin[OCTAVES];
        for (int i = 0; i < OCTAVES; i++) octaves[i] = new Perlin(rand.nextLong());
    }

    public Noise(Rectangle2D.Double size, NoiseTypes noiseType, long seed) {
        this.size = size;
        this.OCTAVES = NoiseBuilder.DEFAULTOCTAVES;
        this.SCALE = NoiseBuilder.DEFAULTSCALE;
        this.ZOOM = NoiseBuilder.DEFAULTZOOM;
        this.LACUNARITY = NoiseBuilder.DEFAULTLACUNARITY;
        this.GAIN = NoiseBuilder.DEFAULTGAIN;
        this.noiseType = noiseType;

        rand = new Random(seed);
        octaves = new Perlin[OCTAVES];
        for (int i = 0; i < OCTAVES; i++) octaves[i] = new Perlin(rand.nextLong());
        Function<Float, Float> x = aFloat -> aFloat*aFloat;
    }

    public double evaluate(double x, double y) {
        double maxAmp = 0;
        double amp = 1;
        double freq = SCALE;
        double val = 0;

        double dx = x / size.width / ZOOM;
        double dy = y / size.width / ZOOM;

        for (Perlin layer : octaves) {
            val += noiseType.inner.transformation(layer.eval(dx*freq, dy*freq)) * amp;
            maxAmp += amp;
            amp *= GAIN;
            freq *= LACUNARITY;
        }
        val /= maxAmp;

        double v = noiseType.outer.transformation(val);
        if (v > max) max = v;
        if (v < min) min = v;

        return v;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }


    enum NoiseTypes {
        NoTransform(new NoTransform(), new NoTransform()),
        Standard(new NoTransform(), new StandardTransform()),
        Billowed(new Billowed(), new NoTransform()),
        Ridged(new Ridged(), new NoTransform()),
        BillowedOuter(new NoTransform(), new Billowed()),
        RidgedOuter(new NoTransform(), new Ridged());

        private noiseTransform inner, outer;

        NoiseTypes(noiseTransform inner, noiseTransform outer) {
            this.inner = inner;
            this.outer = outer;
        }
    }

    interface noiseTransform { double transformation(double r); }

    static class NoTransform implements noiseTransform {
        public double transformation(double r) { return r; }}

    class ScaleTransform implements noiseTransform {
        double oldMin, oldMax, newMin, newMax;
        public double transformation(double r) { return Utilities.normalize(r, oldMin, oldMax, newMin, newMax); }}

    static class StandardTransform implements noiseTransform {
        public double transformation(double r) { return (r+1)/2; }}

    static class Billowed implements noiseTransform {
        public double transformation(double r) { return Math.abs(r); }}

    static class Ridged implements noiseTransform {
        public double transformation(double r) { return 1-Math.abs(r); }}
}

class NoiseBuilder {
    static final int DEFAULTOCTAVES = 8;
    static final int DEFAULTSCALE = 256;
    static final int DEFAULTZOOM = 64;
    static final double DEFAULTLACUNARITY = 1.92, DEFAULTGAIN = 0.5;

    private int OCTAVES = 8;
    private int SCALE = 256;
    private int ZOOM = 64;
    private double LACUNARITY = 1.92, GAIN = 0.5;

    private Rectangle2D.Double mapSize;

    public NoiseBuilder(Rectangle2D.Double mapSize) {
        this.mapSize = mapSize;
    }

    NoiseBuilder setOctaves(int OCTAVES) {
        this.OCTAVES = OCTAVES;
        return this;
    }

    NoiseBuilder setScale(int SCALE) {
        this.SCALE = SCALE;
        return this;
    }

    NoiseBuilder setZoom(int ZOOM) {
        this.ZOOM = ZOOM;
        return this;
    }

    NoiseBuilder setLacunarity(double LACUNARITY) {
        this.LACUNARITY = LACUNARITY;
        return this;
    }

    NoiseBuilder setGain(int GAIN) {
        this.GAIN = GAIN;
        return this;
    }

    Noise build(Noise.NoiseTypes noiseType, long seed) {
        Noise noise = new Noise(mapSize, noiseType, seed, OCTAVES, SCALE, ZOOM, LACUNARITY, GAIN);

        OCTAVES = DEFAULTOCTAVES; SCALE = DEFAULTSCALE; ZOOM = DEFAULTZOOM;
        LACUNARITY = DEFAULTLACUNARITY; GAIN = DEFAULTGAIN;
        return noise;
    }
}
