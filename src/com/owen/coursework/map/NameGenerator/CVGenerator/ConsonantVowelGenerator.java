package com.owen.coursework.map.NameGenerator.CVGenerator;

import com.owen.coursework.Utilities;
import com.owen.coursework.map.NameGenerator.RandomPicker;

import java.util.Random;

public class ConsonantVowelGenerator {
    private Random rand;
    private static RandomPicker vowels, consonants, citySuffixes;
    private int wordLength = 0;

    public ConsonantVowelGenerator(long seed) {
        rand = new Random(seed);
        vowels = new RandomPicker("vowels.txt", rand.nextLong());
        consonants = new RandomPicker("consonants.txt", rand.nextLong());
        citySuffixes = new RandomPicker("CitySynonyms.txt", rand.nextLong());
    }

    private String pickConsonant() {
        String c = consonants.pick();
        wordLength += c.length();
        return c;
    }

    private String pickVowel() {
        String v = vowels.pick();
        wordLength += v.length();
        return v;
    }

    public String generateName(int lb, int ub) {
        int targetLength = lb + rand.nextInt(ub-lb), currentLength = 0;
        char div = rand.nextBoolean() ? ' ' : '-';
        StringBuilder name = new StringBuilder();

        while (currentLength < targetLength-2) {
            int wordLength = generateLength(targetLength-currentLength, 0.9);
            name.append(generateWord(wordLength)).append(div);
            currentLength += wordLength;
            System.out.printf("Target: %d, Current: %d, Word Length: %d, Word: %s\n", targetLength, currentLength, wordLength, name);
        }
        return Utilities.capitalize(name.toString().replaceAll("[^\\w]+$", ""));
    }

    private int generateLength(int n, double p) {
        double x = rand.nextDouble();
        for (int i = 0; i < n; i++)
            if (Math.pow(p, i) < x) return i+1;
        return n;
    }

    public String generateWord(int length) {
//        int length = lb + rand.nextInt(ub-lb);
        boolean vowel = rand.nextBoolean();
        StringBuilder nameBuidler = new StringBuilder();

        wordLength = 0;
        while (nameBuidler.length() < length) {
            if (vowel) nameBuidler.append(pickVowel());
            else {
                if (rand.nextDouble() < 0.8) nameBuidler.append(pickConsonant());
                else {
                    nameBuidler.append(pickVowel());
                    vowel = true;
                }
            }
            vowel = !vowel;

//            if (rand.nextDouble() > Math.pow(0.95, wordLength-1)) {
//                wordLength = 0;
//            }
        }
        String name = nameBuidler.toString();

        // Remove trailing spaces and single letter words
        name = name.replaceAll("([^\\w])\\w([^\\w]|$)", " ").replaceAll("[^\\w]+$", "");

        if (name.length() <= 1) name = generateName();   // Regenerate if too short
        return name;
    }

    public String generateName() {
        return generateName(4, 12);
    }

    public String generateCityName() {
        return rand.nextDouble() < 0.6 ? generateName(5, 10)+citySuffixes.pick(): generateName(6, 14);
    }
    

}
