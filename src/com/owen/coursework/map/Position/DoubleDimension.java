package com.owen.coursework.map.Position;

import java.awt.geom.Dimension2D;
import java.util.Objects;

public class DoubleDimension extends Dimension2D {
    public double width, height;

    public DoubleDimension() {
        this(0, 0);
    }

    public DoubleDimension(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public DoubleDimension(DoubleDimension d) {
        this.width = d.width;
        this.height = d.height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "DoubleDimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleDimension that = (DoubleDimension) o;
        return Double.compare(that.width, width) == 0 &&
                Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}
