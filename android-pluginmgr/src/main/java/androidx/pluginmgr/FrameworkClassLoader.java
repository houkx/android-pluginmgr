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

import android.util.Log;

/**
 * 框架类加载器（Application 的 classLoder被替换成此类的实例）
 * 
 * @author HouKangxi
 *
 */
class FrameworkClassLoader extends ClassLoader {
	private String plugId;
	private String actName;

	public FrameworkClassLoader(ClassLoader parent) {
		super(parent);
	}

	String newActivityClassName(String plugId, String actName) {
		this.plugId = plugId;
		this.actName = actName;
		return ActivityOverider.targetClassName;
	}

	protected Class<?> loadClass(String className, boolean resolv)
			throws ClassNotFoundException {
		Log.i("cl", "loadClass: " + className);
		if (plugId != null) {
			String pluginId = plugId;

			PlugInfo plugin = PluginManager.getInstance().getPluginById(
					pluginId);
			Log.i("cl", "plugin = " + plugin);
			if (plugin != null) {
				try {
					if (className.equals(ActivityOverider.targetClassName)) {
						// Thread.dumpStack();
						String actClassName = actName;
						return plugin.getClassLoader().loadActivityClass(
								actClassName);
					} else {
						return plugin.getClassLoader().loadClass(className);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return super.loadClass(className, resolv);
	}
}