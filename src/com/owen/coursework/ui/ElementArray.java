package com.owen.coursework.ui;

import com.owen.coursework.map.Position.ScreenPosition;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ElementArray extends UIElement {
    public static final int ORIENTATION_VERTICAL = 0, ORIENTATION_HORIZONTAL = 1;

    private UIElement[] elements;

    public ElementArray(Anchor anchor, int x, int y, int separation, int orientation, UIElement... elements) {
        super(anchor, x, y, 100, 100);
        this.elements = elements;
        arrangeButtons(separation, orientation);
    }

    public ElementArray(UIElement... elements) {
        this(Anchor.TopLeft, 0, 0, 0, 0, elements);
    }

    public ElementArray(ArrayList<UIElement> elements) {
        this(elements.toArray(new UIElement[0]));
    }

    @Override
    public void manageControls(double timePassed) {
        super.manageControls(timePassed);
        for (UIElement element : elements) {
            element.manageControls(timePassed);
        }
    }

    @Override
    public void update(double timePassed) {
        super.update(timePassed);
        for (UIElement element : elements) {
            element.update(timePassed);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        for (UIElement element : elements) {
            element.draw(g);
        }
    }

    public void arrangeButtons(int separation, int orientation) {
        if (orientation == ORIENTATION_VERTICAL) {
            int height = separation, width = 0;
            for (UIElement element1 : elements) {         // Sums height and gets max width
                height += element1.size.height + separation;
                width = Math.max(element1.size.width, width);
            }

            size.height = height;
            size.width = width + separation*2;
            int elementY = offset.y + (int) (-anchor.getHeightFraction()*height) + separation;  // TL corner
            int x = (int) (offset.x + (0.5 - anchor.getWidthFraction())*separation);

            for (UIElement element : elements) { // Positions each element
                element.anchor = anchor;
                element.setOffset(new ScreenPosition(x, (int) (elementY + element.size.height * anchor.getHeightFraction())));
                // If element is another array, arrange it horizontally
                if (element instanceof ElementArray)
                    ((ElementArray) element).arrangeButtons(separation, 1 - orientation);
                elementY += element.size.height + separation;
            }

        } else {    // Same as above, but horizontally
            int width = separation, height = 0;
            for (UIElement element1 : elements) {
                width += element1.size.width + separation;
                height = Math.max(element1.size.height, height);
            }

            size.width = width;
            size.height = height + separation*2;
            int elementX = offset.x + (int) (-anchor.getWidthFraction()*width) + separation;
            int y = (int) (offset.y + (0.5 - anchor.getHeightFraction()) * separation);

            for (UIElement element : elements) {
                element.anchor = anchor;
                element.setOffset(new ScreenPosition((int) (elementX + element.size.width * anchor.getWidthFraction()), y));
                if (element instanceof ElementArray)
                    ((ElementArray) element).arrangeButtons(separation, 1 - orientation);
                elementX += element.size.width + separation;
            }
        }
    }

    @Override
    public ElementArray addOffset(ScreenPosition offset) {
        super.addOffset(offset);
        for (UIElement element : elements) {
            element.addOffset(offset);
        }
        return this;
    }

    @Override
    public ElementArray setSize(Dimension size) {
        super.setSize(size);
        return this;
    }

    @Override
    public String toString() {
        return "ElementArray{" +
                "elements=" + Arrays.toString(elements) +
                '}';
    }
}
