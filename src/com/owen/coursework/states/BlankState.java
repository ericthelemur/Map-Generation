package com.owen.coursework.states;

import java.awt.*;

public interface BlankState {
    void onTransfer();
    void manageControls(double timePassed);
    void update(double timePassed);
    void draw(Graphics2D g);
}
