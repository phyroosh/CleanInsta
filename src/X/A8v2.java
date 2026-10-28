package X;

import android.graphics.Typeface;

public class A8v2 {
    public static Typeface a(Object o1, Object o2) {
        if (o2 instanceof Typeface) {
            return (Typeface) o2;
        }
        return null;
    }
}
