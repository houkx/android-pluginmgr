/*
 * Copyright (C) 2015 HouKx <hkx.aidream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
