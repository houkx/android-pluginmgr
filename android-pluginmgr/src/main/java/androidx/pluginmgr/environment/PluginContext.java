package androidx.pluginmgr.environment;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.pluginmgr.delegate.LayoutInflaterProxyContext;

/**
 * @author Lody
 * @version 1.0
 */
public class PluginContext extends LayoutInflaterProxyContext {

    private PlugInfo plugInfo;

    public PluginContext(Context hostContext, PlugInfo plugInfo) {
        super(hostContext);
        if (plugInfo == null) {
            throw new IllegalStateException("Create a plugin context, but not given host context!");
        }
        this.plugInfo = plugInfo;
    }

    @Override
    public Resources getResources() {
        return plugInfo.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return plugInfo.getAssets();
    }

    @Override
    public ClassLoader getClassLoader() {
        return plugInfo.getClassLoader();
    }

    @Override
    public Context getBaseContext() {
        return getBaseContextInner(getBaseContext());
    }

    private Context getBaseContextInner(Context baseContext) {
        if (baseContext instanceof ContextWrapper) {
            return getBaseContextInner(((ContextWrapper) baseContext).getBaseContext());
        }
        return baseContext;
    }
}
