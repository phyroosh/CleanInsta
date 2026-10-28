package com.clean.insta.adblock;

import android.content.Context;
import android.util.Log;
import com.clean.insta.core.CleanPrefs;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AdBlocker: Intercepts feed payloads and network requests to eliminate sponsored content,
 * promotional carousels, and client telemetry.
 */
public final class AdBlocker {
    private static final String TAG = "CleanInsta.AdBlock";

    /**
     * Checks if a network request target domain/URL is telemetry or an advertisement server.
     */
    public static boolean isTelemetryOrAdEndpoint(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();

        return lower.contains("logging_client_events")
                || lower.contains("b-graph.facebook.com")
                || lower.contains("graph.instagram.com/logging")
                || lower.contains("appmetrica")
                || lower.contains("nakheelteam.cc")
                || lower.contains("mobileads")
                || lower.contains("doubleclick.net")
                || lower.contains("analytics");
    }

    /**
     * Sanitizes a Feed JSON response by removing all sponsored and suggested items.
     * @param rawJson The raw JSON string from Instagram feed / timeline API.
     * @return Cleaned JSON string without ads.
     */
    public static String sanitizeFeedResponse(Context context, String rawJson) {
        if (rawJson == null || context == null) return rawJson;
        CleanPrefs prefs = CleanPrefs.get(context);
        if (!prefs.isAdBlockEnabled()) return rawJson;

        try {
            JSONObject root = new JSONObject(rawJson);
            if (root.has("feed_items")) {
                JSONArray items = root.getJSONArray("feed_items");
                JSONArray cleanItems = new JSONArray();

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item == null) continue;

                    // Check for sponsored post flag
                    JSONObject media = item.optJSONObject("media_or_ad");
                    if (media != null) {
                        boolean isSponsored = media.optBoolean("is_sponsored", false)
                                || media.has("ad_id")
                                || media.has("invalidation_rules");
                        if (isSponsored) {
                            Log.d(TAG, "[ADBLOCK] Filtered sponsored feed item");
                            continue;
                        }
                    }

                    // Check for suggested user/shopping carousels
                    if (prefs.getBoolean(CleanPrefs.KEY_REMOVE_SUGGESTIONS, true)) {
                        if (item.has("suggested_users") || item.has("stories_netego")) {
                            Log.d(TAG, "[ADBLOCK] Filtered netego / suggested carousel");
                            continue;
                        }
                    }

                    cleanItems.put(item);
                }

                root.put("feed_items", cleanItems);
                return root.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sanitizing feed JSON: " + e.getMessage());
        }

        return rawJson;
    }
}