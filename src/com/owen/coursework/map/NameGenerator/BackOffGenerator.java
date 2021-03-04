package com.owen.coursework.map.NameGenerator;

import com.owen.coursework.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class BackOffGenerator {
    private final MarkovGenerator[] generators;
    private final String start;

    public BackOffGenerator(int order, ArrayList<String> data, long seed) {
        start = String.join("", Collections.nCopies(order, "^")); // Constructs base string (^^^)
        Random rand = new Random(seed);
        generators = new MarkovGenerator[order];
        for (int i = 0; i < order; i++) generators[i] = new MarkovGenerator(order-i, data, rand.nextLong());
    }

    public BackOffGenerator(int order, String filename, long seed) {
        this(order, MarkovGenerator.readFile(filename), seed);
    }

    public String generate() {
        StringBuilder wordBuilder = new StringBuilder(start);
        String c;
        do {c = getLetter(wordBuilder.toString());  // Add picked letter until reaching end
            wordBuilder.append(c);
        } while (!c.equals("$"));

        String word = wordBuilder.toString();
        word = word.replaceAll("^\\^+|\\$+$", "");  // Remove ^ and $
        if (word.length() < 6 || 12 < word.length()) return generate(); // Regenerate if too long or short
        return Utilities.capitalize(word);

    }

    private String getLetter(String word) {             // Takes letter from highest order generator, but if null...
        String c;                                       // Take letter from the next highest, ensures all words will be
        for (MarkovGenerator generator: generators) {   // Finished when unusual letters come up
            c = generator.pickNext(word);
            if (c.length() > 0) return c;
        }
        return "";
    }
}
