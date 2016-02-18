package androidx.pluginmgr.environment;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.pluginmgr.Globals;
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
        return getBaseContextInner(super.getBaseContext());
    }

    private Context getBaseContextInner(Context baseContext) {
        Context realBaseContext = baseContext;
        while (realBaseContext instanceof ContextWrapper) {
            realBaseContext = ((ContextWrapper) realBaseContext).getBaseContext();
        }
        return realBaseContext;
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equals(Globals.GET_HOST_CONTEXT)) {
            return super.getBaseContext();
        }else if (name.equals(Globals.GET_HOST_RESOURCE)) {
            return plugInfo.getResources();
        }else if (name.equals(Globals.GET_HOST_ASSETS)) {
            return plugInfo.getAssets();
        }else if (name.equals(Globals.GET_HOST_CLASS_LOADER)) {
            return plugInfo.getClassLoader();
        }else if (name.equals(Globals.GET_PLUGIN_PATH)) {
            return plugInfo.getFilePath();
        }else if (name.equals(Globals.GET_PLUGIN_PKG_NAME)) {
            return plugInfo.getPackageName();
        }else if (name.equals(Globals.GET_PLUGIN_PKG_INFO)) {
            return plugInfo.getPackageInfo();
        }
        return super.getSystemService(name);
    }
}
