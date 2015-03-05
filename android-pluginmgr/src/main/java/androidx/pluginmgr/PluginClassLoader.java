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
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import dalvik.system.DexClassLoader;

/**
 * 插件类加载器
 * 
 * @author HouKangxi
 * 
 */
class PluginClassLoader extends DexClassLoader {
	private final String tag;
	private final PlugInfo thisPlugin;
	private final String optimizedDirectory;
	private final String libraryPath;
	/**
	 * Activity 的类加载器
	 */
	private final Map<String, ClassLoader> proxyActivityLoaderMap;

	public PluginClassLoader(String dexPath, String optimizedDir, ClassLoader parent, PlugInfo plugin) {
		super(dexPath, optimizedDir,plugin.getPackageInfo().applicationInfo.nativeLibraryDir,parent);
		thisPlugin = plugin;
		proxyActivityLoaderMap = new HashMap<String, ClassLoader>(plugin.getActivities().size());
		this.libraryPath = plugin.getPackageInfo().applicationInfo.nativeLibraryDir;
		this.optimizedDirectory = optimizedDir;
		tag = "PluginClassLoader( " + plugin.getPackageInfo().packageName + " )";
	}

	Class<?> loadActivityClass(final String actClassName) throws ClassNotFoundException {
		Log.d(tag, "loadActivityClass: " + actClassName);

		// 在类加载之前检查创建代理的Activity dex文件，以免调用者忘记生成此文件
		File dexSavePath = ActivityOverider.createProxyDex(thisPlugin, actClassName, true);
		ClassLoader actLoader = proxyActivityLoaderMap.get(actClassName);
		if (actLoader == null) {
			actLoader = new DexClassLoader(dexSavePath.getAbsolutePath(), optimizedDirectory,libraryPath, this);
			proxyActivityLoaderMap.put(actClassName, actLoader);
		}
		return actLoader.loadClass(ActivityOverider.targetClassName);
	}
	
}
