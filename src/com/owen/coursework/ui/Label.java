package com.owen.coursework.ui;

import com.owen.coursework.Game;

import java.awt.*;

public class Label extends UIElement {
    Color textColour = Color.BLACK;
    public String text;
    Font font = Game.Fonts.smallFont;

    // Constructors
    public Label(String text, Color textColour, Color backgroundColour, Font font, Anchor anchor, int x, int y,
                 int width, int height, int border) {
        super(backgroundColour, anchor, x, y, width, height);
        this.text = wrapText(text, width-border*2, font);
        this.textColour = textColour;
        if (font != null) this.font = font;
    }

    public Label(String text, Anchor anchor, int x, int y, int width) {
        super(anchor, x, y, width, 100);
        this.text = wrapText(text, width-20, null);
        size.height = getTextDimensions().height;
    }

    public Label(String text, Anchor anchor, int x, int y) {
        super(anchor, x, y, 100, 100);
        this.text = text;
        size = getTextDimensions();
    }

    public Label(String text, Font font, int border) {
        this(text, Anchor.MiddleMiddle, 0, 0);
        this.font = font;
        size = getTextDimensions();
        size.height += border*2;
        size.width += border*2;
    }

    public Label(String text, Font font) {
        this(text, Anchor.MiddleMiddle, 0, 0);
        this.font = font;
        size = getTextDimensions();
    }

    public Label(String text) {
        this(text, Anchor.MiddleMiddle, 0, 0);
    }

    private Dimension getTextDimensions() {  // Calculates size of text
        FontMetrics metrics = Game.getGraphics().getFontMetrics(font);
        Dimension dimension = new Dimension(0, 0);

        for (String line : text.split("\n")) {
            dimension.width = Math.max(dimension.width,  metrics.stringWidth(line));
            dimension.height += metrics.getHeight();
        }
        dimension.width += metrics.stringWidth(" ");
        return dimension;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(textColour);
        g.setFont(font);
        drawText(g);

    }

    void drawText(Graphics2D g) {
        String[] strings = text.split("\n");
        FontMetrics metrics = g.getFontMetrics(font);       // Calculates central x value and TL y
        int x = getX() + size.width/2;
        int y = getY() + (int) ((size.height - metrics.getHeight()*strings.length)/2.0) + metrics.getAscent();

        for (int i = 0; i < strings.length; i++) {  // Draws lines
            String s = strings[i];
            g.drawString(s, x - metrics.stringWidth(s)/2, y + i*metrics.getHeight());
        }
    }

    public static String wrapText(String text, int width, Font font) {  // Split at paragraphs
        StringBuilder wrappedText = new StringBuilder();
        for (String line : text.split("\n"))
            wrappedText.append(wrapParagraph(line, width, font)).append("\n");
        wrappedText.deleteCharAt(wrappedText.length()-1);
        return wrappedText.toString();
    }

    private static String wrapParagraph(String para, int width, Font font) { // Split paragraph into lines
        FontMetrics metrics = font != null ? Game.getGraphics().getFontMetrics(font) : Game.getGraphics().getFontMetrics();
        if (metrics.stringWidth(para) <= width) return para;

        String[] words = para.split(" ");

        StringBuilder wrappedPara = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : words) {         // While words fit on line add to line
            if (metrics.stringWidth(line.toString()+word) < width) {
                line.append(word).append(" ");
            }
            else {          // Otherwise, add line to paragraph and create new line
                wrappedPara.append(line).append("\n");
                line = new StringBuilder();
                line.append(word);
            }
        }
        return wrappedPara.append(line.toString()).toString();  // Return string (adds end of line)
    }

    @Override
    public String toString() {
        return "Label{" +
                "text='" + text + '\'' +
                ", offset=" + offset +
                ", size=" + size +
                '}';
    }
}
