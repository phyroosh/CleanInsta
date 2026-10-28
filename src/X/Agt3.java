package X;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;

public class Agt3 {
    private static final String TAG = "CleanInsta.Agt3";

    public static void a(Object obj, int resId, boolean force) {
        if (obj == null || resId == 0) return;
        try {
            if (obj instanceof Resources.Theme) {
                ((Resources.Theme) obj).applyStyle(resId, force);
            } else if (obj instanceof ContextThemeWrapper) {
                ((ContextThemeWrapper) obj).getTheme().applyStyle(resId, force);
            } else if (obj instanceof Context) {
                ((Context) obj).getTheme().applyStyle(resId, force);
            } else {
                try {
                    java.lang.reflect.Method m = obj.getClass().getMethod("applyStyle", int.class, boolean.class);
                    m.invoke(obj, resId, force);
                } catch (NoSuchMethodException ignored) {
                    try {
                        java.lang.reflect.Method mTheme = obj.getClass().getMethod("getTheme");
                        Object theme = mTheme.invoke(obj);
                        if (theme instanceof Resources.Theme) {
                            ((Resources.Theme) theme).applyStyle(resId, force);
                        }
                    } catch (Throwable t2) {
                        Log.w(TAG, "Failed theme reflection fallback on " + obj.getClass().getName() + ": " + t2.getMessage());
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed applying theme style " + resId + ": " + t.getMessage());
        }
    }
}
