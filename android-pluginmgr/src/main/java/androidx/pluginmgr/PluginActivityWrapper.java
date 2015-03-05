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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;

/**
 * @author HouKangxi
 *
 */
class PluginActivityWrapper extends ContextThemeWrapper {
	private static final String tag = "PluginActivityWrapper";
	private PlugInfo plugin;
	private Context appWrapper;
	public PluginActivityWrapper(Context base, Context appWrapper,PlugInfo plugin) {
		attachBaseContext(base);
		this.plugin = plugin;
		this.appWrapper = appWrapper;
	}

//	@Override
//	public Theme getTheme() {
//		Log.d(tag, "getTheme()");
//		return null;
//	}

	@Override
	public File getFilesDir() {
		return appWrapper.getFilesDir();
	}

	@Override
	public String getPackageResourcePath() {
		Log.d(tag, "getPackageResourcePath()");
		return appWrapper.getPackageResourcePath();
	}

	@Override
	public String getPackageCodePath() {
		Log.d(tag, "getPackageCodePath()");
		return appWrapper.getPackageCodePath();
	}

	@Override
	public File getCacheDir() {
		Log.d(tag, "getCacheDir()");
		return appWrapper.getCacheDir();
	}

	@Override
	public PackageManager getPackageManager() {
		Log.d(tag, "PackageManager()");
		return appWrapper.getPackageManager();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return appWrapper.getApplicationInfo();
	}

	@Override
	public Context getApplicationContext() {
		Log.d(tag, "getApplicationContext()");
		return appWrapper.getApplicationContext();
	}

	@Override
	public String getPackageName() {
		Log.d(tag, "getPackageName()");
		return appWrapper.getPackageName();
	}

	@Override
	public Resources getResources() {
		Log.d(tag, "getResources()");
		return appWrapper.getResources();
	}

	@Override
	public AssetManager getAssets() {
		Log.d(tag, "getAssets()");
		return appWrapper.getAssets();
	}
}
