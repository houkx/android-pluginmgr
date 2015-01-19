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
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * 提供公共方法供自动生成的Activity调用
 * 
 * @author HouKangxi
 */
public class ActivityOverider {
	private static final String tag = "ActivityOverider";
	/**
	 * 自动生成的 Activity 的全类名
	 */
	static final String targetClassName = "androidx.pluginmgr.PluginActivity";
    // ------------------- process service  ---------
	/**
	 * 覆盖 StarService 方法
	 * 
	 * @param intent
	 * @param fromAct
	 */
	public static ComponentName overrideStartService(Activity fromAct,String pluginId,Intent intent) {
		//TODO 覆盖 StarService 方法
		Log.d(tag, "overrideStartService");
		return fromAct.startService(intent);
	}
	public static boolean overrideBindService(Activity fromAct,String pluginId,Intent intent,ServiceConnection conn, int flags) {
		//TODO overrideBindService
		Log.d(tag, "overrideBindService");
		return fromAct.bindService(intent, conn, flags);
	}
	public static void overrideUnbindService(Activity fromAct,String pluginId,ServiceConnection conn) {
		//TODO overrideUnbindService
		Log.d(tag, "overrideUnbindService");
		fromAct.unbindService( conn);
	}
	public static boolean overrideStopService(Activity fromAct,String pluginId,Intent intent){
		//TODO overrideStopService
		Log.d(tag, "overrideStopService");
		return fromAct.stopService(intent);
	}
	// ------------------ process Activity ---------------------------
	/**
	 * 处理 插件Activity 通过 intent 跳转到别的Activity
	 * <p>
	 * 供插件中的 startActivity 调用
	 * 
	 * @param fromAct
	 *            - 发出请求的Activity
	 * @param pluginId
	 *            - 插件id
	 * @param intent
	 *            - 启动其他Activity的Intent请求
	 * @param requestCode
	 * @param options
	 * @return 修改后的 Intent
	 */
	public static Intent overrideStartActivityForResult(Activity fromAct, String pluginId,Intent intent, int requestCode,
			Bundle options) {
		// 主要做以下工作：
		// 1 、修改Intent的跳转目标
		// 2 、帮助插件类加载器决定使用哪个activity类加载器
		PluginManager mgr = PluginManager.getInstance();
		// 优先判断类名，若类名为空再判断 Action
		if (intent.getComponent() != null
				&& intent.getComponent().getClassName() != null) {
			// action 为空，但是指定了包名和 activity类名
			ComponentName compname = intent.getComponent();
			String pkg = compname.getPackageName();
			String toActName = compname.getClassName();
			PlugInfo thisPlugin = mgr.getPluginById(pluginId);
			ActivityInfo actInThisApk = null;
			PlugInfo plug = thisPlugin;
			if (pkg != null) {
				if (pkg.equals(thisPlugin.getPackageName())) {
					actInThisApk = thisPlugin
							.findActivityByClassName(toActName);
				}else{
					PlugInfo otherPlug = mgr.getPluginByPackageName(pkg);
					if (otherPlug != null) {
						plug = otherPlug;
						actInThisApk = otherPlug
								.findActivityByClassName(toActName);
					}
				}
			} else {
				actInThisApk = thisPlugin.findActivityByClassName(toActName);
			}
			if (actInThisApk != null) {
				setPluginIntent(intent, plug, actInThisApk.name);
			} else {
				for (PlugInfo plugInfo : mgr.getPlugins()) {
					if (plugInfo == thisPlugin) {
						continue;
					}
					ActivityInfo otherAct = plugInfo
							.findActivityByClassName(toActName);
					if (otherAct != null) {
						setPluginIntent(intent, plugInfo, otherAct.name);
						break;
					}
				}
			}
		} else if (intent.getAction() != null) {
			String action = intent.getAction();
			//
			// 开始处理 action
			// 先判断activity所在的插件有没有对应action,因为绝大多数情况下应用都是在其内部界面之间跳转
			PlugInfo thisPlugin = mgr.getPluginById(pluginId);
			ActivityInfo actInThisApk = thisPlugin.findActivityByAction(action);
			if (actInThisApk != null) {
				setPluginIntent(intent, thisPlugin, actInThisApk.name);
			} else {
				for (PlugInfo plugInfo : mgr.getPlugins()) {
					if (plugInfo == thisPlugin) {
						continue;
					}
					ActivityInfo otherAct = plugInfo
							.findActivityByAction(action);
					if (otherAct != null) {
						setPluginIntent(intent, plugInfo, otherAct.name);
						break;
					}
				}
			}
		}

		return intent;
	}

	private static void setPluginIntent(Intent intent, PlugInfo plugin,
			String actName) {
		PluginManager mgr = PluginManager.getInstance();
		String pluginId = plugin.getId();
		createProxyDex(plugin, actName);
		String act = mgr.getFrameworkClassLoader().newActivityClassName(
				pluginId, actName);
		ComponentName compname = new ComponentName(mgr.getContext(), act);
		intent.setComponent(compname);
	}
	static File getPluginBaseDir(String pluginId) {
		String pluginPath = PluginManager.getInstance()
				.getDexInternalStoragePath().getAbsolutePath();
		String pluginDir = pluginPath + '/' + pluginId + "-dir/";
		File folder = new File(pluginDir);
		folder.mkdirs();
		return folder;
	}
	
	static File getPluginLibDir(String pluginId) {
		File folder = new File(getPluginBaseDir(pluginId)+ "/lib/");
		return folder;
	}
	
	static File getPorxyActivityDexPath(String pluginId, String activity) {
		File folder = new File(getPluginBaseDir(pluginId)+"/activities/");
		folder.mkdirs();
		String suffix = ".dex";
		if (android.os.Build.VERSION.SDK_INT < 11) {
			suffix = ".jar";
		}
		File savePath = new File(folder, activity + suffix);
		return savePath;
	}

	static File createProxyDex(PlugInfo plugin, String activity) {
		return createProxyDex(plugin, activity, true);
	}

	static File createProxyDex(PlugInfo plugin, String activity, boolean lazy) {
		File savePath = getPorxyActivityDexPath(plugin.getId(), activity);
		createProxyDex(plugin, activity, savePath, lazy);
		return savePath;
	}

	private static void createProxyDex(PlugInfo plugin, String activity, File saveDir,
			boolean lazy) {
		// Log.d(tag + ":createProxyDex", "plugin=" + plugin + "\n, activity="
		// + activity);
		if (lazy && saveDir.exists()) {
			// Log.d(tag, "dex alreay exists: " + saveDir);
			// 已经存在就不创建了，直接返回
			return;
		}
		// Log.d(tag, "actName=" + actName + ", saveDir=" + saveDir);
		try {
			String pkgName = plugin.getPackageName();
			ActivityClassGenerator.createActivityDex(activity, targetClassName,
					saveDir, plugin.getId(), pkgName);
		} catch (Throwable e) {
			Log.e(tag, Log.getStackTraceString(e));
		}
	}
	public static Object[] overrideAttachBaseContext(String pluginId, Activity fromAct,Context base){
		Log.i(tag, "overrideAttachBaseContext: pluginId="+pluginId+", activity="+fromAct.getClass().getSuperclass().getName());
		// 
		PlugInfo plugin = PluginManager.getInstance().getPluginById(pluginId);
		try {
			if(plugin.getApplication()==null){
				PluginManager.getInstance().initPluginApplication(plugin, fromAct);
			}
		} catch (Exception e) {
			Log.e(tag, Log.getStackTraceString(e));
		}
		// setTheme TODO 空指针异常 因为此时 mResources 字段还没赋值 暂时转移至onCreate回调中实现
	/*	String actName = fromAct.getClass().getSuperclass().getName();
		Log.d(tag, "pluginId = "+plugin+", actName = "+actName);
		ActivityInfo actInfo = plugin.findActivityByClassName(actName);
		int themeResId = actInfo.theme;
		Log.d(tag,"actTheme="+themeResId);
		if (themeResId == 0) {
			themeResId = plugin.getPackageInfo().applicationInfo.theme;
			Log.d(tag,"applicationTheme="+themeResId);
		}
		if (themeResId != 0) {
			fromAct.setTheme(themeResId);
		}*/
		PluginActivityWrapper actWrapper = new PluginActivityWrapper(base, plugin.appWrapper, plugin);
		return new Object[] { actWrapper, plugin.getAssetManager() };
	}

	/**
	 * 按下back键的方法调用
	 * 
	 * @param pluginId
	 * @param fromAct
	 * @return 是否调用父类的onBackPressed()方法
	 */
	public static boolean overrideOnbackPressed(Activity fromAct,String pluginId) {
		PlugInfo plinfo = PluginManager.getInstance().getPluginById(pluginId);
		String actName = fromAct.getClass().getSuperclass().getName();
		ActivityInfo actInfo = plinfo.findActivityByClassName(actName);
		boolean finish = plinfo.isFinishActivityOnbackPressed(actInfo);
		if (finish) {
			fromAct.finish();
		}
		boolean ivsuper = plinfo.isInvokeSuperOnbackPressed(actInfo);
		Log.d(tag, "finish? " + finish + ", ivsuper? " + ivsuper);
		return ivsuper;
	}

	//
	// =================== Activity 生命周期回调方法 ==================
	//
	public static void callback_onCreate(String pluginId, Activity fromAct) {
		PluginManager con = PluginManager.getInstance();
		PlugInfo plugin = con.getPluginById(pluginId);
		// replace Application
		try {
			Field applicationField = Activity.class
					.getDeclaredField("mApplication");
			applicationField.setAccessible(true);
			applicationField.set(fromAct, plugin.getApplication());
		}  catch (Exception e) {
			e.printStackTrace();
		}
		String actName = fromAct.getClass().getSuperclass().getName();
		Log.d(tag, "pluginId = "+plugin+", actName = "+actName);
		ActivityInfo actInfo = plugin.findActivityByClassName(actName);
		int themeResId = actInfo.theme;
		Log.d(tag,"actTheme="+themeResId);
		if (themeResId == 0) {
			themeResId = plugin.getPackageInfo().applicationInfo.theme;
			Log.d(tag,"applicationTheme="+themeResId);
		}
		if (themeResId != 0) {
			fromAct.setTheme(themeResId);
		}
		// 如果是三星Galaxy S4 手机，则使用包装的LayoutInflater替换原LayoutInflater
		// 这款手机在解析内置的布局文件时有各种错误
		if (android.os.Build.MODEL.equals("GT-I9500")) {
			Window window = fromAct.getWindow();// 得到 PhoneWindow 实例
			try {
				Object origInf = ReflectionUtils.getFieldValue(window, "mLayoutInflater");
				if(!(origInf instanceof LayoutInflaterWrapper)){
					ReflectionUtils.setFieldValue(window, "mLayoutInflater",
							new LayoutInflaterWrapper(window.getLayoutInflater()));
				}
			} catch (Exception e) {
				Log.e(tag, Log.getStackTraceString(e));
			}
		}
		// invoke callback
		PluginActivityLifeCycleCallback callback = con
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onCreate(pluginId, fromAct);
		}
	}

	public static void callback_onResume(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onResume(pluginId, fromAct);
		}
	}

	public static void callback_onStart(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onStart(pluginId, fromAct);
		}
	}

	public static void callback_onRestart(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onRestart(pluginId, fromAct);
		}
	}

	public static void callback_onPause(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onPause(pluginId, fromAct);
		}
	}

	public static void callback_onStop(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onStop(pluginId, fromAct);
		}
	}

	public static void callback_onDestroy(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginManager.getInstance()
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onDestroy(pluginId, fromAct);
		}
	}
}
