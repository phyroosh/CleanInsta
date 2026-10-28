package com.clean.insta.downloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MediaDownloader: Clean, non-blocking media downloader for Reels, Stories, Posts, and Audio.
 */
public final class MediaDownloader {
    private static final String TAG = "CleanInsta.Download";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public interface DownloadCallback {
        void onProgress(int percentage);
        void onSuccess(File savedFile);
        void onError(Exception error);
    }

    /**
     * Downloads media from a direct CDN URL and saves it to public storage.
     */
    public static void downloadMedia(Context context, String mediaUrl, String filename, DownloadCallback callback) {
        if (mediaUrl == null || context == null) {
            if (callback != null) callback.onError(new IllegalArgumentException("Invalid media URL or Context"));
            return;
        }

        EXECUTOR.execute(() -> {
            InputStream in = null;
            FileOutputStream out = null;
            HttpURLConnection conn = null;

            try {
                // Determine target directory (Movies for video, Pictures for photos)
                String subFolder = filename.endsWith(".mp4") ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
                File baseDir = Environment.getExternalStoragePublicDirectory(subFolder);
                File cleanDir = new File(baseDir, "CleanInsta");
                if (!cleanDir.exists()) cleanDir.mkdirs();

                File targetFile = new File(cleanDir, filename);

                URL url = new URL(mediaUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; K) AppleWebKit/537.36");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IllegalStateException("HTTP error code: " + responseCode);
                }

                int totalLength = conn.getContentLength();
                in = conn.getInputStream();
                out = new FileOutputStream(targetFile);

                byte[] buffer = new byte[8192];
                int read;
                long downloaded = 0;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    downloaded += read;
                    if (totalLength > 0 && callback != null) {
                        int progress = (int) ((downloaded * 100) / totalLength);
                        callback.onProgress(progress);
                    }
                }
                out.flush();

                // Scan file so it immediately appears in the Android Gallery / MediaStore
                MediaScannerConnection.scanFile(context, new String[]{targetFile.getAbsolutePath()}, null, null);

                Log.d(TAG, "Download finished: " + targetFile.getAbsolutePath());
                if (callback != null) callback.onSuccess(targetFile);

            } catch (Exception e) {
                Log.e(TAG, "Download failed for " + mediaUrl + ": " + e.getMessage());
                if (callback != null) callback.onError(e);
            } finally {
                try { if (in != null) in.close(); } catch (Exception ignored) {}
                try { if (out != null) out.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        });
    }
}