package com.openlycatholic.app.missal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Locale;

final class UsccbReadingsLoader {

    private UsccbReadingsLoader() {
    }

    static Readings parse(String response) throws JSONException, IOException {
        Object decoded = new JSONTokener(response).nextValue();
        if (decoded instanceof String) {
            decoded = new JSONTokener((String) decoded).nextValue();
        }
        if (!(decoded instanceof JSONObject)) {
            throw new IOException(
                    "Unexpected USCCB page response: "
                            + response.substring(0, Math.min(response.length(), 120))
            );
        }

        JSONObject page = (JSONObject) decoded;
        JSONObject mass = new JSONObject();
        JSONArray sections = page.getJSONArray("sections");
        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String key = getReadingKey(section.optString("heading", ""));
            String text = section.optString("text", "").trim();
            if (key == null || text.isEmpty()) {
                continue;
            }

            JSONObject reading = new JSONObject();
            reading.put("reference", section.optString("reference", "").trim());
            reading.put("text", text);
            mass.put(key, reading);
        }

        if (!mass.has("first_reading") || !mass.has("gospel")) {
            throw new IOException(
                    "USCCB page loaded without its reading sections (found "
                            + sections.length()
                            + " sections)."
            );
        }

        String title = page.optString("title", "Daily Readings").trim();
        return new Readings(
                title.isEmpty() ? "Daily Readings" : title,
                mass
        );
    }

    private static String getReadingKey(String heading) {
        String normalized = heading.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("reading 1") || normalized.equals("first reading")) {
            return "first_reading";
        }
        if (normalized.startsWith("reading 2") || normalized.equals("second reading")) {
            return "second_reading";
        }
        if (normalized.startsWith("responsorial psalm")) {
            return "psalm";
        }
        if (normalized.equals("alleluia") || normalized.equals("gospel acclamation")) {
            return "alleluia";
        }
        if (normalized.equals("gospel")) {
            return "gospel";
        }
        return null;
    }

    static final class Readings {
        final String title;
        final JSONObject mass;

        Readings(String title, JSONObject mass) {
            this.title = title;
            this.mass = mass;
        }
    }
}
