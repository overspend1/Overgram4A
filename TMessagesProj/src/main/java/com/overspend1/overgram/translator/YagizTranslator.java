/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 */

package com.overspend1.overgram.translator;

import android.text.TextUtils;

import com.exteragram.messenger.utils.TranslatorUtils;
import com.overspend1.overgram.OverConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LanguageDetector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * YagizTranslator - Specialized Turkish ↔ English translation with multiple API support
 *
 * Supports:
 * - Gemini API (high quality, contextual)
 * - Google Translate (fast, reliable)
 * - DeepL API (premium quality)
 */
public class YagizTranslator {

    public static final int API_GEMINI = 0;
    public static final int API_GOOGLE = 1;
    public static final int API_DEEPL = 2;

    public interface OnTranslationSuccess {
        void run(String translatedText, String detectedLanguage);
    }

    public interface OnTranslationFail {
        void run(String error);
    }

    /**
     * Auto-detect language and translate Turkish ↔ English
     */
    public static void translateAuto(String text, OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        if (TextUtils.isEmpty(text)) {
            if (onFail != null) {
                onFail.run("Empty text");
            }
            return;
        }

        // Detect language first
        if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(text, detectedLang -> {
                if (detectedLang == null || detectedLang.equals("und")) {
                    // Unknown language, try to detect from content
                    detectedLang = detectLanguageFromContent(text);
                }

                String targetLang;
                if (detectedLang.startsWith("tr")) {
                    targetLang = "en"; // Turkish → English
                } else {
                    targetLang = "tr"; // Anything else → Turkish
                }

                translate(text, detectedLang, targetLang, onSuccess, onFail);
            }, e -> {
                FileLog.e("YagizTranslator: Language detection failed", e);
                // Fallback: detect from content
                String detectedLang = detectLanguageFromContent(text);
                String targetLang = detectedLang.equals("tr") ? "en" : "tr";
                translate(text, detectedLang, targetLang, onSuccess, onFail);
            });
        } else {
            // No language detection support, detect from content
            String detectedLang = detectLanguageFromContent(text);
            String targetLang = detectedLang.equals("tr") ? "en" : "tr";
            translate(text, detectedLang, targetLang, onSuccess, onFail);
        }
    }

    /**
     * Translate with explicit source and target languages
     */
    public static void translate(String text, String sourceLang, String targetLang,
                                 OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        if (TextUtils.isEmpty(text)) {
            if (onFail != null) {
                onFail.run("Empty text");
            }
            return;
        }

        int apiType = OverConfig.yagizTranslatorApiType;

        switch (apiType) {
            case API_GEMINI:
                translateWithGemini(text, sourceLang, targetLang, onSuccess, onFail);
                break;
            case API_DEEPL:
                translateWithDeepL(text, sourceLang, targetLang, onSuccess, onFail);
                break;
            case API_GOOGLE:
            default:
                translateWithGoogle(text, sourceLang, targetLang, onSuccess, onFail);
                break;
        }
    }

    /**
     * Translate using Gemini API (contextual, high quality)
     */
    private static void translateWithGemini(String text, String sourceLang, String targetLang,
                                           OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        if (TextUtils.isEmpty(OverConfig.geminiApiKey)) {
            FileLog.d("YagizTranslator: No Gemini API key, falling back to Google");
            translateWithGoogle(text, sourceLang, targetLang, onSuccess, onFail);
            return;
        }

        new Thread(() -> {
            try {
                String apiKey = OverConfig.geminiApiKey;
                String model = OverConfig.geminiModel;
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

                String sourceLangName = sourceLang.equals("tr") ? "Turkish" : "English";
                String targetLangName = targetLang.equals("tr") ? "Turkish" : "English";

                String prompt = String.format(
                    "Translate the following text from %s to %s. " +
                    "Provide ONLY the translated text, no explanations or additional text.\n\n" +
                    "Text: %s",
                    sourceLangName, targetLangName, text
                );

                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String translatedText = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim();

                    if (onSuccess != null) {
                        AndroidUtilities.runOnUIThread(() -> onSuccess.run(translatedText, sourceLang));
                    }
                } else {
                    throw new Exception("Gemini API error: " + responseCode);
                }
            } catch (Exception e) {
                FileLog.e("YagizTranslator: Gemini translation failed", e);
                // Fallback to Google Translate
                translateWithGoogle(text, sourceLang, targetLang, onSuccess, onFail);
            }
        }).start();
    }

    /**
     * Translate using Google Translate (fast, reliable)
     */
    private static void translateWithGoogle(String text, String sourceLang, String targetLang,
                                           OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        // Use existing TranslatorUtils from exteragram
        TranslatorUtils.translate(text, sourceLang, targetLang, translatedText -> {
            if (onSuccess != null) {
                onSuccess.run(translatedText.toString(), sourceLang);
            }
        }, () -> {
            if (onFail != null) {
                onFail.run("Google Translate failed");
            }
        });
    }

    /**
     * Translate using DeepL API (premium quality)
     */
    private static void translateWithDeepL(String text, String sourceLang, String targetLang,
                                          OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        // DeepL API requires API key - for now, fallback to Google
        // User can implement this with their own DeepL API key
        FileLog.d("YagizTranslator: DeepL not yet implemented, falling back to Google");
        translateWithGoogle(text, sourceLang, targetLang, onSuccess, onFail);
    }

    /**
     * Simple language detection from content
     */
    private static String detectLanguageFromContent(String text) {
        if (TextUtils.isEmpty(text)) {
            return "en";
        }

        // Turkish-specific characters
        String turkishChars = "çÇğĞıİöÖşŞüÜ";
        int turkishCharCount = 0;

        for (char c : text.toCharArray()) {
            if (turkishChars.indexOf(c) >= 0) {
                turkishCharCount++;
            }
        }

        // If more than 2% of characters are Turkish-specific, it's likely Turkish
        double ratio = (double) turkishCharCount / text.length();
        return ratio > 0.02 ? "tr" : "en";
    }

    /**
     * Check if text is likely Turkish
     */
    public static boolean isTurkish(String text) {
        return detectLanguageFromContent(text).equals("tr");
    }

    /**
     * Get API name for display
     */
    public static String getApiName(int apiType) {
        switch (apiType) {
            case API_GEMINI:
                return "Gemini AI";
            case API_DEEPL:
                return "DeepL";
            case API_GOOGLE:
            default:
                return "Google Translate";
        }
    }
}
