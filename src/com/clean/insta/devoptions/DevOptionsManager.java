package com.clean.insta.devoptions;

import android.content.Context;
import android.util.Log;
import com.clean.insta.core.CleanPrefs;

/**
 * DevOptionsManager: Enables and controls internal Instagram developer options and QE flags.
 */
public final class DevOptionsManager {
    private static final String TAG = "CleanInsta.Dev";

    /**
     * Determines whether developer options menu and experiment flags should be exposed.
     */
    public static boolean isDevOptionsEnabled(Context context) {
        if (context == null) return false;
        return CleanPrefs.get(context).isDevOptionsEnabled();
    }

    /**
     * Overrides internal boolean configuration values (MobileConfig / QE flags).
     * @param paramName The name of the MobileConfig parameter.
     * @param originalValue The default value provided by Meta servers.
     * @return The overridden boolean value.
     */
    public static boolean overrideBooleanParam(Context context, String paramName, boolean originalValue) {
        if (!isDevOptionsEnabled(context) || paramName == null) return originalValue;

        // Custom feature flag overrides
        if (paramName.contains("is_employee") || paramName.contains("is_internal_build")) {
            return true;
        }

        if (paramName.contains("direct_unsend_visual_indicator")) {
            // Keep deleted messages visible
            return true;
        }

        return originalValue;
    }
}