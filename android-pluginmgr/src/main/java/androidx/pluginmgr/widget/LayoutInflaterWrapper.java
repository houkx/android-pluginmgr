package androidx.pluginmgr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;


public class LayoutInflaterWrapper extends LayoutInflater {

    private static final String TAG = "LayoutInflaterWrapper";

    private LayoutInflater target;
    private final Class<?> layoutClass;
    private final Class<?> idClass;
    @SuppressWarnings("unused")
    private final Class<?> attrClass;
    private final int screenTitle;

    public LayoutInflaterWrapper(LayoutInflater target) {
        super(target.getContext());
        this.target = target;
        Class<?> layoutClass = null;
        Class<?> idClass = null;
        Class<?> attrClass = null;
        int screenTitle = 0;
        try {
            layoutClass = Class.forName("com.android.internal.R$layout");
            idClass = Class.forName("com.android.internal.R$id");
            attrClass = Class.forName("com.android.internal.R$attr");
            screenTitle = layoutClass.getField("screenTitle").getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.layoutClass = layoutClass;
        this.idClass = idClass;
        this.attrClass = attrClass;
        this.screenTitle = screenTitle;
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return target.cloneInContext(newContext);
    }

    @Override
    public Context getContext() {
        return target.getContext();
    }

    @Override
    public void setFactory(Factory factory) {
        target.setFactory(factory);
    }

    @Override
    public Filter getFilter() {
        return target.getFilter();
    }

    @Override
    public void setFilter(Filter filter) {
        target.setFilter(filter);
    }

    @Override
    public View inflate(int resource, ViewGroup root) {
        Log.i(TAG, "inflate布局( resource=" + resource + ", root=" + root + " )");
        if (resource == screenTitle) {
            Log.i(TAG, "使用自定义布局");
            return createLayoutScreenSimple(resource, root);
        }
        return target.inflate(resource, root);
    }

    private View createLayoutScreenSimple(int resource, ViewGroup root) {
        LinearLayout lyt = new LinearLayout(getContext());
        lyt.setOrientation(LinearLayout.VERTICAL);
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            try {
                LinearLayout.class.getMethod("setFitsSystemWindows",
                        boolean.class).invoke(lyt, true);
            } catch (Throwable ignored) {
            }
        }
        int viewStubId = 0;
        int frameLytId = 0;
        int layoutResource = 0;
        int inflatedId = 0;
        try {
            frameLytId = idClass.getField("content").getInt(null);
            viewStubId = idClass.getField("action_mode_bar_stub").getInt(null);
            inflatedId = idClass.getField("action_mode_bar").getInt(null);
            layoutResource = layoutClass.getField("action_mode_bar").getInt(
                    null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        {
            ViewStub viewStub = new ViewStub(
                    getContext());
            viewStub.setId(viewStubId);
            if (inflatedId != 0)
                viewStub.setInflatedId(inflatedId);
            if (layoutResource != 0)
                viewStub.setLayoutResource(layoutResource);
            lyt.addView(viewStub, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        FrameLayout flyt_content = new FrameLayout(getContext());
        flyt_content.setId(frameLytId);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        flyt_content
                .setForegroundGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP);
        lyt.addView(flyt_content, layoutParams);
        return lyt;
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root) {
        return target.inflate(parser, root);
    }

    @Override
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return target.inflate(resource, root, attachToRoot);
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root,
                        boolean attachToRoot) {
        return target.inflate(parser, root, attachToRoot);
    }

    @Override
    protected View onCreateView(String name, AttributeSet attrs)
            throws ClassNotFoundException {
        try {
            return (View) LayoutInflater.class.getDeclaredMethod(
                    "onCreateView", String.class, AttributeSet.class).invoke(
                    target, name, attrs);
        } catch (Exception e) {
            e.printStackTrace();
            return super.onCreateView(name, attrs);
        }
    }

    protected View onCreateView(View parent, String name, AttributeSet attrs)
            throws ClassNotFoundException {
        try {
            return (View) LayoutInflater.class.getDeclaredMethod(
                    "onCreateView", View.class, String.class,
                    AttributeSet.class).invoke(target, parent, name, attrs);
        } catch (Exception e) {
            e.printStackTrace();
            return super.onCreateView(name, attrs);
        }
    }

}
