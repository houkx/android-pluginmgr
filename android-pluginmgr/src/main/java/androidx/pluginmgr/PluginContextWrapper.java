/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

/**
 * Plugin Context 包装类
 * 
 * @author HouKangxi
 *
 */
class PluginContextWrapper extends ContextWrapper {
	private PlugInfo plugin;
	private static final String tag = "PluginContextWrapper";
	private ApplicationInfo applicationInfo;
	private File fileDir;
	public PluginContextWrapper(Context base, PlugInfo plugin) {
		super(base);
		this.plugin = plugin;
		applicationInfo = new ApplicationInfo(super.getApplicationInfo());
		applicationInfo.sourceDir = plugin.getFilePath();
		applicationInfo.dataDir = ActivityOverider.getPluginBaseDir(
				plugin.getId()).getAbsolutePath();
		fileDir = new File(ActivityOverider.getPluginBaseDir(plugin.getId())
				.getAbsolutePath() + "/files/");
	}

	@Override
	public File getFilesDir() {
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return fileDir;
	}

	@Override
	public String getPackageResourcePath() {
		// TODO Auto-generated method stub
		Log.d(tag, "getPackageResourcePath()");
		return super.getPackageResourcePath();
	}

	@Override
	public String getPackageCodePath() {
		// TODO Auto-generated method stub
		Log.d(tag, "getPackageCodePath()");
		return super.getPackageCodePath();
	}

	@Override
	public File getCacheDir() {
		// TODO Auto-generated method stub
		Log.d(tag, "getCacheDir()");
		return super.getCacheDir();
	}

	@Override
	public PackageManager getPackageManager() {
		// TODO Auto-generated method stub
		Log.d(tag, "PackageManager()");
		return super.getPackageManager();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	@Override
	public Context getApplicationContext() {
		Log.d(tag, "getApplicationContext()");
		return this;
	}

	@Override
	public String getPackageName() {
		Log.d(tag, "getPackageName()");
		return plugin.getPackageName();
	}

	@Override
	public Resources getResources() {
		Log.d(tag, "getResources()");
		return plugin.getResources();
	}

	@Override
	public AssetManager getAssets() {
		Log.d(tag, "getAssets()");
		return plugin.getAssetManager();
	}
	// @Override
	// public Object getSystemService(String name) {
	// if (name.equals(Context.ACTIVITY_SERVICE)) {
	// if (plugin.getApplicationInfo().process != null) {
	// return plugin.activityManager;
	// }
	// }
	// return super.getSystemService(name);
	// }
}
