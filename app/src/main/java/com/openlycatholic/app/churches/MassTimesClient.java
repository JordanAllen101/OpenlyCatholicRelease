package com.openlycatholic.app.churches;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class MassTimesClient {

    private static final double SEARCH_RADIUS_MILES = 100.0;

    private MassTimesClient() {
    }

    static List<Church> parseNearby(String response, double latitude, double longitude)
            throws JSONException {
        List<Church> churches = new ArrayList<>();
        JSONArray responseArray = new JSONArray(response);
        for (int index = 0; index < responseArray.length(); index++) {
            Church church = parseChurch(responseArray.getJSONObject(index));
            if (isFinite(church.latitude)
                    && isFinite(church.longitude)
                    && isFinite(latitude)
                    && isFinite(longitude)) {
                church.distanceMiles = distanceMiles(
                        latitude,
                        longitude,
                        church.latitude,
                        church.longitude
                );
            }
            if (church.distanceMiles <= SEARCH_RADIUS_MILES) {
                churches.add(church);
            }
        }
        return churches;
    }

    private static Church parseChurch(JSONObject json) throws JSONException {
        Church church = new Church();
        church.name = readString(json, "name", "Catholic church");
        church.type = readString(json, "church_type_name", "");
        church.city = readString(json, "church_address_city_name", "");
        church.region = readString(json, "church_address_providence_name", "");
        church.country = readString(json, "church_address_country_territory_name", "");
        church.streetAddress = readString(json, "church_address_street_address", "");
        church.postalCode = readString(json, "church_address_postal_code", "");
        church.phone = readString(json, "phone_number", "");
        church.website = readString(json, "url", "");
        church.latitude = json.optDouble("latitude", Double.NaN);
        church.longitude = json.optDouble("longitude", Double.NaN);
        church.distanceMiles = Double.NaN;

        JSONArray services = json.optJSONArray("church_worship_times");
        if (services != null) {
            for (int index = 0; index < services.length(); index++) {
                JSONObject service = services.getJSONObject(index);
                String kind = service.optString("service_typename", "").trim();
                String scheduleType = getScheduleType(kind);
                if (scheduleType == null) {
                    continue;
                }

                Schedule schedule = new Schedule();
                schedule.type = scheduleType;
                schedule.day = readString(service, "day_of_week", "");
                schedule.start = readString(service, "time_start", "");
                schedule.end = readString(service, "time_end", "");
                schedule.language = readString(service, "language", "");
                schedule.comment = readString(service, "comment", "");
                church.schedules.add(schedule);
            }
        }
        return church;
    }

    private static String getScheduleType(String serviceType) {
        switch (serviceType) {
            case "Weekend":
            case "Week Days":
                return "Mass";
            case "Confessions":
                return "Confession";
            case "Adorations":
                return "Adoration";
            default:
                return null;
        }
    }

    private static String readString(JSONObject json, String key, String fallback) {
        return json.isNull(key) ? fallback : json.optString(key, fallback).trim();
    }

    static String formatTime(String value) {
        String time = value == null ? "" : value.trim();
        if (time.isEmpty()) {
            return "";
        }

        String[] inputPatterns = {"HH:mm:ss", "HH:mm", "h:mm a", "h:mm:ss a"};
        for (String pattern : inputPatterns) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
            parser.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            Date parsed = parser.parse(time, position);
            if (parsed != null && position.getIndex() == time.length()) {
                return new SimpleDateFormat("h:mm a", Locale.US).format(parsed);
            }
        }
        return time;
    }

    private static double distanceMiles(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude
    ) {
        double latitudeDifference = Math.toRadians(endLatitude - startLatitude);
        double longitudeDifference = Math.toRadians(endLongitude - startLongitude);
        double haversine = Math.sin(latitudeDifference / 2)
                * Math.sin(latitudeDifference / 2)
                + Math.cos(Math.toRadians(startLatitude))
                * Math.cos(Math.toRadians(endLatitude))
                * Math.sin(longitudeDifference / 2)
                * Math.sin(longitudeDifference / 2);
        return 3958.8 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    static final class Church {
        String name;
        String type;
        String city;
        String region;
        String country;
        String streetAddress;
        String postalCode;
        String phone;
        String website;
        double latitude;
        double longitude;
        double distanceMiles;
        final List<Schedule> schedules = new ArrayList<>();
    }

    static final class Schedule {
        String type;
        String day;
        String start;
        String end;
        String language;
        String comment;
    }
}
