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

import android.app.Activity;

/**
 * 插件Activity的生命周期方法回调
 * 
 * @author HouKangxi
 * 
 */
public interface PluginActivityLifeCycleCallback {

	public void onCreate(String pluginId, Activity pluginAct);

	public void onResume(String pluginId, Activity pluginAct);

	public void onPause(String pluginId, Activity pluginAct);

	public void onStart(String pluginId, Activity pluginAct);

	public void onRestart(String pluginId, Activity pluginAct);

	public void onStop(String pluginId, Activity pluginAct);

	public void onDestroy(String pluginId, Activity pluginAct);

	// public boolean onBackPressed(String pluginId, Activity pluginAct);
}
