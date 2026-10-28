package com.instagram.app;

import android.content.Context;
import android.util.Log;
import com.clean.insta.core.HookManager;

/**
 * CleanInsta Application entry point.
 * Replaces Honista App with clean, malware-free initialization.
 */
public class App extends InstagramAppShell {
    private static final String TAG = "CleanInsta.App";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.i(TAG, "CleanInsta attachBaseContext: Dextricks & secondary DEX initialized.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CleanInsta onCreate: Booting CleanInsta privacy and security shield.");
        HookManager.init(this);
    }
}
