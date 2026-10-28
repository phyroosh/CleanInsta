package com.clean.insta.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.clean.insta.adblock.AdBlocker;
import com.clean.insta.devoptions.DevOptionsManager;
import com.clean.insta.downloader.MediaDownloader;
import com.clean.insta.privacy.GhostMode;
import com.clean.insta.privacy.MessageVault;
import com.clean.insta.ui.CleanSettingsActivity;

/**
 * HookManager: The central, unified hook gateway for CleanInsta.
 * Connects intercepted Instagram runtime events to our clean modules without any external dependencies.
 */
public final class HookManager {
    private static final String TAG = "CleanInsta.Hook";

    /**
     * Initializes CleanInsta core services, pre-warms database and settings.
     */
    public static void init(Context context) {
        if (context == null) return;
        Log.i(TAG, "CleanInsta v1.0 core engine initialized.");
        CleanPrefs.get(context);
        MessageVault.get(context);
    }

    /**
     * Intercepts outgoing HTTP requests.
     * @return true to DROP / CANCEL the request (for ghost mode or ad/telemetry blocking).
     */
    public static boolean shouldCancelRequest(Context context, String url) {
        if (url == null) return false;

        // 1. Block tracking, analytics, and telemetry
        if (AdBlocker.isTelemetryOrAdEndpoint(url)) {
            Log.d(TAG, "Suppressed telemetry target: " + url);
            return true;
        }

        // 2. Block seen receipts if ghost mode is active
        if (GhostMode.shouldBlockRequest(context, url)) {
            Log.d(TAG, "Suppressed ghost-mode receipt: " + url);
            return true;
        }

        return false;
    }

    /**
     * Intercepts incoming HTTP responses for feed/explore/stories to strip ads.
     */
    public static String processResponse(Context context, String url, String responseBody) {
        if (url == null || responseBody == null) return responseBody;

        if (url.contains("/api/v1/feed/timeline/") || url.contains("/feed/timeline")) {
            return AdBlocker.sanitizeFeedResponse(context, responseBody);
        }

        return responseBody;
    }

    /**
     * Intercepts incoming direct messages to store them in the Anti-Delete MessageVault.
     */
    public static void onMessageReceived(Context context, String msgId, String threadId, String senderId, String text, String rawJson, long timestamp) {
        if (context == null || msgId == null) return;
        if (CleanPrefs.get(context).isAntiDeleteEnabled()) {
            MessageVault.get(context).recordMessage(msgId, threadId, senderId, text, rawJson, timestamp);
            Log.d(TAG, "Captured incoming message into MessageVault: " + msgId);
        }
    }

    /**
     * Intercepts unsend/delete events in Direct Messages.
     */
    public static void onMessageDeleted(Context context, String msgId) {
        if (context == null || msgId == null) return;
        if (CleanPrefs.get(context).isAntiDeleteEnabled()) {
            MessageVault.get(context).markDeleted(msgId);
            Log.d(TAG, "Flagged unsent message in MessageVault: " + msgId);
        }
    }

    /**
     * Triggers media download cleanly without watermarks or bloat.
     */
    public static void triggerDownload(Context context, String mediaUrl, String filename) {
        MediaDownloader.downloadMedia(context, mediaUrl, filename, new MediaDownloader.DownloadCallback() {
            @Override
            public void onProgress(int percentage) {}

            @Override
            public void onSuccess(java.io.File savedFile) {
                Log.i(TAG, "Saved media to: " + savedFile.getAbsolutePath());
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Media download error: " + error.getMessage());
            }
        });
    }

    /**
     * Intercepts MobileConfig / Quick Experiment boolean parameters.
     */
    public static boolean checkExperimentParam(Context context, String paramName, boolean defaultValue) {
        return DevOptionsManager.overrideBooleanParam(context, paramName, defaultValue);
    }

    /**
     * Opens the CleanInsta settings dashboard.
     */
    public static void launchSettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, CleanSettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}