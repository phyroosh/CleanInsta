package io.appmetrica.analytics.internal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Neutralized telemetry service stub.
 * Immediately terminates with zero network or tracking activity.
 */
public class AppMetricaService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
