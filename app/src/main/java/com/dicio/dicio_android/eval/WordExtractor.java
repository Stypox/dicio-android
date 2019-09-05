package com.dicio.dicio_android.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordExtractor {

    private static String normalizeWord(String word) {
        try {
            if(word.isEmpty()) {
                return null;
            }

            // TODO is it ok to use ROOT?
            return word.toLowerCase(Locale.ROOT);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static List<String> extractWords(String input) {
        // match all non-letter characters
        String[] splitInput = input.split("[^\\p{L}]+");

        List<String> words = new ArrayList<>();
        for(String word : splitInput) {
            String normalized = normalizeWord(word);
            if (normalized != null) {
                words.add(normalized);
            }
        }

        return words;
    }
}
