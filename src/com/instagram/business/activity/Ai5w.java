package com.instagram.business.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.clean.insta.core.HookManager;
import com.clean.insta.ui.CleanSettingsActivity;

/**
 * Compatible replacement for Honista Ai5w launcher entry point.
 * Initializes CleanInsta privacy hooks and launches Instagram without malware.
 */
public class Ai5w extends Activity {
    private static final String TAG = "CleanInsta.Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting CleanInsta core environment...");

        // 1. Initialize CleanInsta hooks and settings
        HookManager.init(getApplicationContext());

        // 2. Check if user or intent requested settings directly
        Intent intent = getIntent();
        if (intent != null && ("com.clean.insta.ACTION_SETTINGS".equals(intent.getAction()) ||
                               intent.getBooleanExtra("open_settings", false))) {
            Intent settingsIntent = new Intent(this, CleanSettingsActivity.class);
            startActivity(settingsIntent);
            finish();
            return;
        }

        // 3. Launch official Instagram root InstagramMainActivity
        try {
            Intent mainIntent = new Intent();
            mainIntent.setClassName(this, "com.instagram.mainactivity.InstagramMainActivity");
            mainIntent.setAction(Intent.ACTION_MAIN);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to launch InstagramMainActivity: " + t.getMessage());
            try {
                Intent fallback = new Intent();
                fallback.setClassName(this, "com.instagram.mainactivity.InstagramMainActivity");
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            } catch (Throwable t2) {
                Log.e(TAG, "Fallback launcher failed: " + t2.getMessage());
                Intent settings = new Intent(this, CleanSettingsActivity.class);
                startActivity(settings);
            }
        }

        finish();
    }
}
