package com.vocabularyai.listening.service;

import com.vocabularyai.listening.dto.WordMatchResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListeningScoringHelper {

    private static final Map<String, String> CONTRACTIONS = new LinkedHashMap<>();
    private static final Map<Integer, String> NUMBERS = new HashMap<>();

    static {
        CONTRACTIONS.put("won't", "will not");
        CONTRACTIONS.put("can't", "cannot");
        CONTRACTIONS.put("shan't", "shall not");
        CONTRACTIONS.put("let's", "let us");
        CONTRACTIONS.put("it's", "it is");
        CONTRACTIONS.put("that's", "that is");
        CONTRACTIONS.put("there's", "there is");
        CONTRACTIONS.put("what's", "what is");
        CONTRACTIONS.put("who's", "who is");
        CONTRACTIONS.put("how's", "how is");
        CONTRACTIONS.put("where's", "where is");
        CONTRACTIONS.put("here's", "here is");
        CONTRACTIONS.put("he's", "he is");
        CONTRACTIONS.put("she's", "she is");
        CONTRACTIONS.put("n't", " not");
        CONTRACTIONS.put("'m", " am");
        CONTRACTIONS.put("'re", " are");
        CONTRACTIONS.put("'ve", " have");
        CONTRACTIONS.put("'ll", " will");
        CONTRACTIONS.put("'d", " would");

        NUMBERS.put(0, "zero");
        NUMBERS.put(1, "one");
        NUMBERS.put(2, "two");
        NUMBERS.put(3, "three");
        NUMBERS.put(4, "four");
        NUMBERS.put(5, "five");
        NUMBERS.put(6, "six");
        NUMBERS.put(7, "seven");
        NUMBERS.put(8, "eight");
        NUMBERS.put(9, "nine");
        NUMBERS.put(10, "ten");
        NUMBERS.put(11, "eleven");
        NUMBERS.put(12, "twelve");
        NUMBERS.put(13, "thirteen");
        NUMBERS.put(14, "fourteen");
        NUMBERS.put(15, "fifteen");
        NUMBERS.put(16, "sixteen");
        NUMBERS.put(17, "seventeen");
        NUMBERS.put(18, "eighteen");
        NUMBERS.put(19, "nineteen");
        NUMBERS.put(20, "twenty");
        NUMBERS.put(30, "thirty");
        NUMBERS.put(40, "forty");
        NUMBERS.put(50, "fifty");
        NUMBERS.put(60, "sixty");
        NUMBERS.put(70, "seventy");
        NUMBERS.put(80, "eighty");
        NUMBERS.put(90, "ninety");
        NUMBERS.put(100, "hundred");
    }

    public static class Token {
        public final String original;
        public final String normalized;

        public Token(String original, String normalized) {
            this.original = original;
            this.normalized = normalized;
        }
    }

    public static class ScoringResult {
        public final int score;
        public final int totalOriginalWords;
        public final int correctWords;
        public final int nearMissWords;
        public final List<WordMatchResult> wordResults;

        public ScoringResult(int score, int totalOriginalWords, int correctWords, int nearMissWords, List<WordMatchResult> wordResults) {
            this.score = score;
            this.totalOriginalWords = totalOriginalWords;
            this.correctWords = correctWords;
            this.nearMissWords = nearMissWords;
            this.wordResults = wordResults;
        }
    }

    public static String expandContractionsAndNormalizeText(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        String text = input.trim()
                .replace("’", "'")
                .replace("‘", "'")
                .replace("`", "'")
                .replace("“", "\"")
                .replace("”", "\"");

        String lower = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : CONTRACTIONS.entrySet()) {
            lower = lower.replace(entry.getKey(), entry.getValue());
        }

        // Convert digits 0-99
        Pattern digitPattern = Pattern.compile("\\b(\\d{1,3})\\b");
        Matcher matcher = digitPattern.matcher(lower);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                int num = Integer.parseInt(matcher.group(1));
                if (num <= 100) {
                    String word = numberToWords(num);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(word));
                    continue;
                }
            } catch (Exception ignored) {}
            matcher.appendReplacement(sb, matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String numberToWords(int n) {
        if (NUMBERS.containsKey(n)) {
            return NUMBERS.get(n);
        }
        if (n > 20 && n < 100) {
            int tens = (n / 10) * 10;
            int ones = n % 10;
            return NUMBERS.getOrDefault(tens, "") + " " + NUMBERS.getOrDefault(ones, "");
        }
        return String.valueOf(n);
    }

    public static List<Token> tokenize(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Split raw text by whitespace
        String[] rawTokens = rawText.trim().split("\\s+");
        List<Token> tokens = new ArrayList<>();

        for (String rt : rawTokens) {
            String cleaned = rt.replaceAll("[^a-zA-Z0-9']", "");
            if (cleaned.isEmpty()) {
                continue;
            }
            String norm = expandContractionsAndNormalizeText(cleaned);
            // In case norm expanded to multiple words (e.g. "don't" -> "do not")
            String[] parts = norm.trim().split("\\s+");
            if (parts.length > 1) {
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i].replaceAll("[^a-zA-Z0-9]", "");
                    if (!part.isEmpty()) {
                        tokens.add(new Token(i == 0 ? rt : "(" + part + ")", part));
                    }
                }
            } else {
                String single = norm.replaceAll("[^a-zA-Z0-9]", "");
                if (!single.isEmpty()) {
                    tokens.add(new Token(rt, single));
                }
            }
        }
        return tokens;
    }

    public static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[] dp = new int[len2 + 1];

        for (int j = 0; j <= len2; j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            int prevDiagonal = dp[0];
            dp[0] = i;
            for (int j = 1; j <= len2; j++) {
                int temp = dp[j];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[j] = prevDiagonal;
                } else {
                    dp[j] = 1 + Math.min(prevDiagonal, Math.min(dp[j - 1], dp[j]));
                }
                prevDiagonal = temp;
            }
        }
        return dp[len2];
    }

    public static boolean isNearMiss(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return false;
        }
        int minLen = Math.min(s1.length(), s2.length());
        if (minLen >= 5) {
            return levenshteinDistance(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT)) <= 1;
        }
        return false;
    }

    public static ScoringResult evaluate(String originalPassage, String userAnswer) {
        List<Token> pTokens = tokenize(originalPassage);
        List<Token> uTokens = tokenize(userAnswer);

        int n = pTokens.size();
        int m = uTokens.size();

        if (n == 0) {
            return new ScoringResult(0, 0, 0, 0, Collections.emptyList());
        }

        if (m == 0) {
            List<WordMatchResult> results = new ArrayList<>();
            for (Token pt : pTokens) {
                results.add(WordMatchResult.builder()
                        .originalWord(pt.original)
                        .userWord(null)
                        .status("MISSING")
                        .build());
            }
            return new ScoringResult(0, n, 0, 0, results);
        }

        // Needleman-Wunsch Alignment
        double MATCH_SCORE = 3.0;
        double NEAR_MISS_SCORE = 2.0;
        double MISMATCH_SCORE = -1.0;
        double GAP_PENALTY = -1.5;

        double[][] dp = new double[n + 1][m + 1];
        int[][] backtrack = new int[n + 1][m + 1]; // 1: diagonal, 2: up (gap U), 3: left (gap P)

        for (int i = 1; i <= n; i++) {
            dp[i][0] = i * GAP_PENALTY;
            backtrack[i][0] = 2;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = j * GAP_PENALTY;
            backtrack[0][j] = 3;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                String pNorm = pTokens.get(i - 1).normalized;
                String uNorm = uTokens.get(j - 1).normalized;

                double sim;
                if (pNorm.equalsIgnoreCase(uNorm)) {
                    sim = MATCH_SCORE;
                } else if (isNearMiss(pNorm, uNorm)) {
                    sim = NEAR_MISS_SCORE;
                } else {
                    sim = MISMATCH_SCORE;
                }

                double diag = dp[i - 1][j - 1] + sim;
                double up = dp[i - 1][j] + GAP_PENALTY;
                double left = dp[i][j - 1] + GAP_PENALTY;

                if (diag >= up && diag >= left) {
                    dp[i][j] = diag;
                    backtrack[i][j] = 1;
                } else if (up >= left) {
                    dp[i][j] = up;
                    backtrack[i][j] = 2;
                } else {
                    dp[i][j] = left;
                    backtrack[i][j] = 3;
                }
            }
        }

        // Backtrack
        List<WordMatchResult> reversedResults = new ArrayList<>();
        int i = n;
        int j = m;

        int correctWords = 0;
        int nearMissWords = 0;

        while (i > 0 || j > 0) {
            int step = backtrack[i][j];
            if (i > 0 && j > 0 && step == 1) {
                Token pt = pTokens.get(i - 1);
                Token ut = uTokens.get(j - 1);
                String pNorm = pt.normalized;
                String uNorm = ut.normalized;

                if (pNorm.equalsIgnoreCase(uNorm)) {
                    correctWords++;
                    reversedResults.add(WordMatchResult.builder()
                            .originalWord(pt.original)
                            .userWord(ut.original)
                            .status("CORRECT")
                            .build());
                } else if (isNearMiss(pNorm, uNorm)) {
                    nearMissWords++;
                    reversedResults.add(WordMatchResult.builder()
                            .originalWord(pt.original)
                            .userWord(ut.original)
                            .status("NEAR_MISS")
                            .build());
                } else {
                    reversedResults.add(WordMatchResult.builder()
                            .originalWord(pt.original)
                            .userWord(ut.original)
                            .status("INCORRECT")
                            .build());
                }
                i--;
                j--;
            } else if (i > 0 && (j == 0 || step == 2)) {
                Token pt = pTokens.get(i - 1);
                reversedResults.add(WordMatchResult.builder()
                        .originalWord(pt.original)
                        .userWord(null)
                        .status("MISSING")
                        .build());
                i--;
            } else {
                Token ut = uTokens.get(j - 1);
                reversedResults.add(WordMatchResult.builder()
                        .originalWord(null)
                        .userWord(ut.original)
                        .status("EXTRA")
                        .build());
                j--;
            }
        }

        Collections.reverse(reversedResults);

        double effectiveCorrect = correctWords + (nearMissWords * 0.8);
        int denominator = Math.max(n, m);
        int finalScore = (int) Math.round((effectiveCorrect / denominator) * 100);
        finalScore = Math.max(0, Math.min(100, finalScore));

        return new ScoringResult(finalScore, n, correctWords, nearMissWords, reversedResults);
    }
}

