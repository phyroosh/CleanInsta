package X;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class A93f extends FrameLayout {
    public View a;

    public A93f(Context c) { super(c); }
    public A93f(Context c, AttributeSet a) { super(c, a); }
    public A93f(Context c, AttributeSet a, int i) { super(c, a, i); }
    public A93f(Context c, AttributeSet a, int i1, int i2) { super(c, a, i1, i2); }

    public void a(View v) {}

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        a(child);
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
    }
}
