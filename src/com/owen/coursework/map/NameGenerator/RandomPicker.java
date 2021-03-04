package com.owen.coursework.map.NameGenerator;

import com.owen.coursework.map.NameGenerator.CVGenerator.ConsonantVowelGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomPicker {
    HashMap<String, Integer> frequencies;
    private long sum = 0;
    private Random rand;

    public RandomPicker(String path, long seed) {
        frequencies = readFile(path);
        rand = new Random(seed);
    }

    public RandomPicker(HashMap<String, Integer> data, long seed) {
        frequencies = data;
        rand = new Random(seed);
    }

    private HashMap<String, Integer> readFile(String fileName) {        // Read file with frequencies listed as: A=1200
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ConsonantVowelGenerator.class.getResourceAsStream(fileName)));
            String line;
            HashMap<String, Integer> mapping = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                if (line.contains("=")) {
                    String[] strings = line.split("=");
                    mapping.put(strings[0], Integer.parseInt(strings[1]));
                    sum += Integer.parseInt(strings[1]);
                }
            }
            return mapping;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public String pick() {
        long p = (long) (sum * rand.nextDouble()), s = 0;
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (p < (s += entry.getValue())) return entry.getKey();
        }
        return "";
    }

    public void inc(String string, int amt) {
        if (!frequencies.containsKey(string)) frequencies.put(string, amt);
        else frequencies.put(string, frequencies.get(string) + amt);
        sum += amt;
    }

    public void inc(String string) {
        inc(string, 1);
    }

    @Override
    public String toString() {
        return "RandomPicker{" + frequencies + '}';
    }
}
