package X;

import java.util.Collections;
import java.util.List;

public class A42v {
    public static List a(Object o) {
        if (o instanceof List) {
            return (List) o;
        }
        return Collections.emptyList();
    }
}
