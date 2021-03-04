package com.owen.coursework.map.NameGenerator;

import com.owen.coursework.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MarkovGenerator {
    private HashMap<String, RandomPicker> chains = new HashMap<>();
    private int order;
    private Random rand;
    private String start;

    public MarkovGenerator(int order, ArrayList<String> data, long seed) {
        this.order = order;
        start = String.join("", Collections.nCopies(order, "^")); // Constructs base string (^^^)
        rand = new Random(seed);
        train(data);
    }

    public MarkovGenerator(int order, String fileName, long seed) {
        this(order, readFile(fileName), seed);
    }

    public static ArrayList<String> readFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(MarkovGenerator.class.getResourceAsStream(fileName)));
            String line;                                                // Get file
            ArrayList<String> list = new ArrayList<>();
            while ((line = reader.readLine()) != null) {        // For each line
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                list.add(line);                 // Add if not comment
            }
            return list;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void train(ArrayList<String> data) {
        for (String word: data) {
            word = start + word + "$";
            for (int i = order; i < word.length(); i++) { // For each set of letters
                String key = word.substring(i-order, i);
                String val = String.valueOf(word.charAt(i));
                if (!chains.containsKey(key)) chains.put(key, new RandomPicker(new HashMap<>(), rand.nextLong()));
                chains.get(key).inc(val);                       // If picker doesn't exist, create a blank picker
                        // Add val
            }
        }
    }

    public String generate() {
        StringBuilder wordBuilder = new StringBuilder(start);
        String c;
        do {c = pickNext(wordBuilder.toString());
            wordBuilder.append(c);
        } while (!c.equals("$"));

        String word = wordBuilder.toString();
        word = word.replaceAll("^\\^+|\\$+$", "");  // Remove leading ^'s and trailing $'s
        if (word.length() < 6 || 12 < word.length()) return generate();
        return Utilities.capitalize(word);
    }

    public String pickNext(String word) {
        RandomPicker chain = chains.get(word.substring(word.length()-order));
        if (chain != null && chain.frequencies.size() > 0) return chain.pick();
        return "";
    }
}
