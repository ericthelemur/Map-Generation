package com.owen.coursework.states;

import com.owen.coursework.Game;
import com.owen.coursework.ui.Anchor;
import com.owen.coursework.ui.Button;
import com.owen.coursework.ui.ElementArray;
import com.owen.coursework.ui.Label;
import com.owen.coursework.ui.DigitBox;
import com.owen.coursework.ui.UIElement;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class MenuState implements BlankState {
    private ArrayList<UIElement> uiElements = new ArrayList<>();
    private DigitBox seedBox;

    public MenuState() {
        seedBox = new DigitBox("", () -> {
            Game.setState(Game.States.GameState);
            Game.generateMap(seedBox.text.length() > 0 ? Long.parseLong(seedBox.text) : new Random().nextLong());
        }, Color.BLACK, Color.WHITE, null, Anchor.MiddleMiddle, 0, 0, 170, 20, 0);

        uiElements.add(     // Constructs menu items
                new ElementArray(Anchor.MiddleMiddle, 0, 0, 10, ElementArray.ORIENTATION_VERTICAL,
                    new Label("Island Generator", Game.Fonts.XLFont),
                    new Label("by Owen Connors"),
                    new UIElement(Anchor.MiddleMiddle, 0, 0, 10, 50), // Spacer

                    new Button("Random Map", () -> {
                        Game.setState(Game.States.GameState);
                        Game.generateMap(new Random().nextLong());
                    }),

                    new UIElement(Anchor.MiddleMiddle, 0, 0, 10, 30),
                    new Label("Enter Seed (0-9 only):"),

                    new ElementArray(seedBox,
                            new Button("C", seedBox::copyToClipboard),
                            new Button("P", seedBox::pasteFromClipboard)),
                    new Button("Generate Map", seedBox.onEnter),
                    new UIElement(Anchor.MiddleMiddle, 0, 0, 10, 100)
                ));
    }

    @Override
    public void onTransfer() {}

    @Override
    public void manageControls(double timePassed) {
        for (UIElement element : uiElements) element.manageControls(timePassed);
    }

    @Override
    public void update(double timePassed) {
        for (UIElement element : uiElements) element.update(timePassed);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 128, 64));
        g.fillRect(0, 0, Game.screenSize.width, Game.screenSize.height);
        for (UIElement element : uiElements) element.draw(g);
    }
}
