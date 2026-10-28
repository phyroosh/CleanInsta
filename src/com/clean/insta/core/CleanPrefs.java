package com.clean.insta.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * CleanPrefs: Zero-bloat, secure preference manager for CleanInsta.
 * Stores all user configuration locally on the device without any external telemetry or pings.
 */
public final class CleanPrefs {
    private static final String PREF_NAME = "clean_insta_prefs";

    // Privacy & Ghost Mode Keys
    public static final String KEY_GHOST_STORIES = "ghost_stories";
    public static final String KEY_GHOST_DIRECT = "ghost_direct";
    public static final String KEY_GHOST_LIVE = "ghost_live";
    public static final String KEY_GHOST_TYPING = "ghost_typing";
    public static final String KEY_ANTI_DELETE = "anti_delete_messages";

    // Ad-Blocking & Anti-Bloat Keys
    public static final String KEY_BLOCK_FEED_ADS = "block_feed_ads";
    public static final String KEY_BLOCK_STORY_ADS = "block_story_ads";
    public static final String KEY_BLOCK_REEL_ADS = "block_reel_ads";
    public static final String KEY_BLOCK_EXPLORE_ADS = "block_explore_ads";
    public static final String KEY_REMOVE_SHOPPING = "remove_shopping";
    public static final String KEY_REMOVE_SUGGESTIONS = "remove_suggestions";

    // Downloader Keys
    public static final String KEY_DOWNLOAD_PATH = "download_directory";
    public static final String KEY_DOWNLOAD_HIGH_RES = "download_high_res";
    public static final String KEY_AUTO_AUDIO_EXTRACT = "auto_audio_extract";

    // Developer & Experiment Keys
    public static final String KEY_DEV_OPTIONS_ENABLED = "dev_options_enabled";
    public static final String KEY_FORCE_DARK_MODE = "force_dark_mode";
    public static final String KEY_AMOLED_BLACK = "amoled_black";

    private final SharedPreferences prefs;
    private static volatile CleanPrefs instance;

    private CleanPrefs(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static CleanPrefs get(Context context) {
        if (instance == null) {
            synchronized (CleanPrefs.class) {
                if (instance == null) {
                    instance = new CleanPrefs(context);
                }
            }
        }
        return instance;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public boolean isGhostStoriesEnabled() {
        return getBoolean(KEY_GHOST_STORIES, true);
    }

    public boolean isGhostDirectEnabled() {
        return getBoolean(KEY_GHOST_DIRECT, true);
    }

    public boolean isGhostLiveEnabled() {
        return getBoolean(KEY_GHOST_LIVE, true);
    }

    public boolean isAntiDeleteEnabled() {
        return getBoolean(KEY_ANTI_DELETE, true);
    }

    public boolean isAdBlockEnabled() {
        return getBoolean(KEY_BLOCK_FEED_ADS, true);
    }

    public boolean isDevOptionsEnabled() {
        return getBoolean(KEY_DEV_OPTIONS_ENABLED, true);
    }
}