package com.owen.coursework.ui;

import com.owen.coursework.Listener;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class DigitBox extends Label {
    public Runnable onEnter;

    public DigitBox(String text, Runnable onEnter, Color textColour, Color backgroundColour, Font font, Anchor anchor,
                    int x, int y, int width, int height, int border) {
        super(text, textColour, backgroundColour, font, anchor, x, y, width, height, border);
        this.onEnter = onEnter;
    }

    public DigitBox(String text, Anchor anchor, int x, int y, int width) {
        super(text, anchor, x, y, width);
    }

    @Override
    public void manageControls(double timePassed) {
        super.manageControls(timePassed);

        for (int kc : Listener.getPressed()) {
            String c = KeyEvent.getKeyText(kc);
            if (c.matches("[0-9]") && text.length() <= 18) text += c;  // Definitely won't be bigger than Long's max
            else if (c.equals("Minus") && text.length() == 0) text = "-";
            else if (kc == KeyEvent.VK_BACK_SPACE && text.length() > 0) text = text.substring(0, text.length()-1);
            else if (kc == KeyEvent.VK_ENTER) onEnter.run();
        }
    }

    // http://avajava.com/tutorials/lessons/how-do-i-copy-a-string-to-the-clipboard.html
    public void copyToClipboard() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(text);
        clipboard.setContents(strSel, null);
    }

    // http://avajava.com/tutorials/lessons/how-do-i-get-a-string-from-the-clipboard.html
    public void pasteFromClipboard() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        try {
            text += ((String) clipboard.getData(DataFlavor.stringFlavor)).replaceAll("\\D+","");
            if (text.length() > 18) text = text.substring(0, 18);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }
}
