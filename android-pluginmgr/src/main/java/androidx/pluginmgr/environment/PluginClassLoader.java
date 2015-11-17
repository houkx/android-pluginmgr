package androidx.pluginmgr.environment;

import android.annotation.TargetApi;
import android.os.Build;

import dalvik.system.DexClassLoader;

/**
 * @author Lody
 * @version 1.0
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PluginClassLoader extends DexClassLoader {

    protected PlugInfo plugInfo;

    public PluginClassLoader(PlugInfo plugInfo, String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.plugInfo = plugInfo;
    }

    public PlugInfo getPlugInfo() {
        return plugInfo;
    }
}
