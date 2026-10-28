package com.clean.insta.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clean.insta.core.CleanPrefs;

/**
 * CleanSettingsActivity: Beautiful, lightweight, native AMOLED settings interface.
 */
public class CleanSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup(this);
    }

    public static void setup(final Activity activity) {
        CleanPrefs prefs = CleanPrefs.get(activity);

        // Root container: Pure AMOLED Black background (#000000)
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setBackgroundColor(Color.BLACK);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 64);
        scrollView.addView(root);

        // Header Title
        TextView title = new TextView(activity);
        title.setText("CleanInsta Settings");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 16, 0, 8);
        root.addView(title);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Pure, Private, Ad-Free & Zero-Bloat Instagram");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#9E9E9E"));
        subtitle.setPadding(0, 0, 0, 48);
        root.addView(subtitle);

        // Section: Ghost Mode & Privacy
        addSectionHeader(activity, root, "GHOST MODE & PRIVACY");
        addToggle(activity, root, "Ghost Stories", "View stories and highlights without leaving seen receipts",
                CleanPrefs.KEY_GHOST_STORIES, prefs.isGhostStoriesEnabled(), prefs);

        addToggle(activity, root, "Ghost Direct Messages", "Read DMs without sending read/seen marks",
                CleanPrefs.KEY_GHOST_DIRECT, prefs.isGhostDirectEnabled(), prefs);

        addToggle(activity, root, "Ghost Live Stream", "Join live broadcasts anonymously without presence broadcast",
                CleanPrefs.KEY_GHOST_LIVE, prefs.isGhostLiveEnabled(), prefs);

        addToggle(activity, root, "Anti-Delete Messages", "Save and display unsent/deleted messages from chat partners",
                CleanPrefs.KEY_ANTI_DELETE, prefs.isAntiDeleteEnabled(), prefs);

        // Section: Ad-Blocker & Anti-Bloat
        addSectionHeader(activity, root, "AD-BLOCKING & FILTERS");
        addToggle(activity, root, "Block Feed Ads", "Strip sponsored posts and carousel advertisements from feed",
                CleanPrefs.KEY_BLOCK_FEED_ADS, prefs.isAdBlockEnabled(), prefs);

        addToggle(activity, root, "Block Story & Reel Ads", "Eliminate promotional video cards from Stories and Reels",
                CleanPrefs.KEY_BLOCK_STORY_ADS, prefs.getBoolean(CleanPrefs.KEY_BLOCK_STORY_ADS, true), prefs);

        addToggle(activity, root, "Remove Suggested Carousels", "Hide suggested users and recommended shopping grids",
                CleanPrefs.KEY_REMOVE_SUGGESTIONS, prefs.getBoolean(CleanPrefs.KEY_REMOVE_SUGGESTIONS, true), prefs);

        // Section: Developer Tools & Media
        addSectionHeader(activity, root, "DEVELOPER & MEDIA OPTIONS");
        addToggle(activity, root, "Enable Internal Developer Options", "Unlock internal Instagram experiment & MobileConfig menu",
                CleanPrefs.KEY_DEV_OPTIONS_ENABLED, prefs.isDevOptionsEnabled(), prefs);

        addToggle(activity, root, "High Quality Downloads", "Download photos and reels at original maximum resolution",
                CleanPrefs.KEY_DOWNLOAD_HIGH_RES, prefs.getBoolean(CleanPrefs.KEY_DOWNLOAD_HIGH_RES, true), prefs);

        // Footer info
        TextView footer = new TextView(activity);
        footer.setText("CleanInsta v1.0 • Built with Pair Programming\nNo Trackers • No Remote Dynamic Loaders • 100% Clean");
        footer.setTextSize(12);
        footer.setGravity(Gravity.CENTER);
        footer.setTextColor(Color.parseColor("#616161"));
        footer.setPadding(0, 64, 0, 32);
        root.addView(footer);

        activity.setContentView(scrollView);
    }

    private static void addSectionHeader(Context context, LinearLayout parent, String title) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextSize(12);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(Color.parseColor("#00E676")); // Vibrant clean green
        header.setPadding(0, 32, 0, 16);
        parent.addView(header);
    }

    private static void addToggle(final Context context, LinearLayout parent, final String title, String description, final String prefKey, boolean defaultValue, final CleanPrefs prefs) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 20, 0, 20);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textCol.setLayoutParams(textParams);

        TextView label = new TextView(context);
        label.setText(title);
        label.setTextSize(16);
        label.setTextColor(Color.WHITE);

        TextView desc = new TextView(context);
        desc.setText(description);
        desc.setTextSize(13);
        desc.setTextColor(Color.parseColor("#888888"));

        textCol.addView(label);
        textCol.addView(desc);

        Switch toggle = new Switch(context);
        toggle.setChecked(defaultValue);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setBoolean(prefKey, isChecked);
                Toast.makeText(context, title + ": " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            }
        });

        row.addView(textCol);
        row.addView(toggle);

        parent.addView(row);

        // Divider
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#1C1C1C"));
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        parent.addView(divider);
    }
}