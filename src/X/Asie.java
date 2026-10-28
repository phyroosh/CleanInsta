package X;

import java.io.InputStream;

public class Asie {
    public static InputStream a(Object o1, Object o2, Object o3) {
        if (o3 instanceof InputStream) {
            return (InputStream) o3;
        }
        return null;
    }
}
