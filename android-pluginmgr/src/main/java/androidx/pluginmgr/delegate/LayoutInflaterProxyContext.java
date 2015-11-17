package androidx.pluginmgr.delegate;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;

/**
 * @author Lody
 * @version 1.0
 */
public class LayoutInflaterProxyContext extends ContextWrapper {

    private LayoutInflater mInflater;

    public LayoutInflaterProxyContext(Context base) {
        super(base);
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return super.getSystemService(name);
    }
}
