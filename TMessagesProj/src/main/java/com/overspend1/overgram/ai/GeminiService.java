package com.overspend1.overgram.ai;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Minimal Gemini client for prompting and translations.
 */
public class GeminiService {
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface Callback {
        void onSuccess(String text);
        void onError(String error);
    }

    public void ask(String prompt, String apiKey, String model, Callback callback) {
        if (TextUtils.isEmpty(apiKey)) {
            callback.onError("Gemini API key missing");
            return;
        }
        if (TextUtils.isEmpty(model)) {
            model = "gemini-2.5-flash";
        }
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", prompt);
        parts.add(text);
        JsonObject item = new JsonObject();
        item.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(item);
        content.add("contents", contents);

        RequestBody body = RequestBody.create(content.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                callback.onError("HTTP " + response.code());
                return;
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            String parsed = parseText(responseBody);
            if (parsed == null) {
                callback.onError("No content");
            } else {
                callback.onSuccess(parsed.trim());
            }
        } catch (IOException e) {
            callback.onError(e.getMessage());
        }
    }

    private String parseText(String response) {
        try {
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                return null;
            }
            JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
            if (content == null) {
                return null;
            }
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (JsonElement part : parts) {
                JsonObject p = part.getAsJsonObject();
                if (p.has("text")) {
                    sb.append(p.get("text").getAsString());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
