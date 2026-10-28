package com.clean.insta.privacy;

import android.content.Context;
import android.util.Log;
import com.clean.insta.core.CleanPrefs;

/**
 * GhostMode: Filters network requests and interactions to prevent read receipts,
 * story view events, live presence, and typing indicators from being sent to Instagram servers.
 */
public final class GhostMode {
    private static final String TAG = "CleanInsta.Ghost";

    /**
     * Determines whether a given HTTP request URL should be blocked to preserve privacy.
     * @param url The full URL or endpoint being requested.
     * @return true if the request should be suppressed (blocked), false to allow it.
     */
    public static boolean shouldBlockRequest(Context context, String url) {
        if (url == null || context == null) return false;
        CleanPrefs prefs = CleanPrefs.get(context);

        // 1. Stories Ghost Mode
        if (prefs.isGhostStoriesEnabled()) {
            if (url.contains("/api/v1/media/seen/") 
                || url.contains("/stories/reel/seen")
                || url.contains("seen_state")) {
                Log.d(TAG, "[GHOST] Blocked Story Seen Receipt: " + url);
                return true;
            }
        }

        // 2. Direct Messages Ghost Mode (Read Receipts)
        if (prefs.isGhostDirectEnabled()) {
            if (url.contains("/api/v1/direct_v2/threads/") && url.contains("/mark_seen")) {
                Log.d(TAG, "[GHOST] Blocked Direct Message Read Receipt: " + url);
                return true;
            }
        }

        // 3. Live Stream Ghost Mode (Presence & Viewers List)
        if (prefs.isGhostLiveEnabled()) {
            if (url.contains("/api/v1/live/") && (url.contains("/heartbeat") || url.contains("/join"))) {
                Log.d(TAG, "[GHOST] Blocked Live Presence Heartbeat: " + url);
                return true;
            }
        }

        // 4. Typing Indicator Ghost Mode
        if (prefs.getBoolean(CleanPrefs.KEY_GHOST_TYPING, true)) {
            if (url.contains("/api/v1/direct_v2/threads/") && url.contains("/activity_status_update")) {
                Log.d(TAG, "[GHOST] Blocked Typing Indicator: " + url);
                return true;
            }
        }

        return false;
    }
}