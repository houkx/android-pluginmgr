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
import android.view.ContextThemeWrapper;

/**
 * 提供公共方法供自动生成的Activity调用
 * 
 * @author HouKangxi
 * @version 1.0
 */
public class ActivityOverider {
	private static final String tag = "ActivityOverider";
	/**
	 * 自动生成的 Activity 的全类名
	 */
	static final String targetClassName = "androidx.pluginmgr.PluginActivity";
	static final String PLUGIN_ID = "_pluginId";
	static final String PLUGIN_ACTIVITY = "_targetAct";
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
		ComponentName compname = new ComponentName(mgr.getContext(), targetClassName);
		intent.setComponent(compname)
		 .putExtra(ActivityOverider.PLUGIN_ID, pluginId)
		 .putExtra(ActivityOverider.PLUGIN_ACTIVITY, actName);
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
		File savePath = new File(folder,  
				String.format("%s-%d%s",activity, ActivityClassGenerator.VERSION_CODE,suffix));
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
	public static Object[] overrideAttachBaseContext(final String pluginId,final Activity fromAct,Context base){
	
		Log.i(tag, "overrideAttachBaseContext: pluginId="+pluginId+", activity="+fromAct.getClass().getSuperclass().getName()
				);
		// 
		PlugInfo plugin = PluginManager.getInstance().getPluginById(pluginId);
		if (plugin.getApplication() == null) {
			try {
				PluginManager.getInstance().initPluginApplication(plugin,
						null);
			} catch (Exception e) {
				Log.e(tag, Log.getStackTraceString(e));
			}
		}
		try {
			Object loadedApk = ReflectionUtils.getFieldValue(base,"mPackageInfo");
			Log.d(tag, "loadedApk = "+loadedApk);
			ReflectionUtils.setFieldValue(loadedApk, "mClassLoader",
					plugin.getClassLoader());
		} catch (Exception e) {
			e.printStackTrace();
		}
		PluginActivityWrapper actWrapper = new PluginActivityWrapper(base, plugin.appWrapper, plugin);
		return new Object[] { actWrapper, plugin.getAssetManager() };
	}
	
	private static void changeActivityInfo(Context activity){
		final String actName = activity.getClass().getSuperclass().getName();
		Log.d(tag, "changeActivityInfo: activity = "+activity+", class = "+actName);
		if(!activity.getClass().getName().equals(targetClassName)){
			Log.w(tag, "not a Proxy Activity ,then return.");
			return;
		}
		ActivityInfo origActInfo = null;
		try {
			Field field_mActivityInfo = Activity.class.getDeclaredField("mActivityInfo");
			field_mActivityInfo.setAccessible(true);
			origActInfo = (ActivityInfo) field_mActivityInfo.get(activity);
		}  catch (Exception e) {
			Log.e(tag, Log.getStackTraceString(e));
			return;
		}
		PluginManager con = PluginManager.getInstance();
		PlugInfo plugin = con.getPluginByPackageName(activity.getPackageName());
		
		ActivityInfo actInfo = plugin.findActivityByClassName(actName);
		actInfo.applicationInfo = plugin.getPackageInfo().applicationInfo;
		if (origActInfo != null) {
			origActInfo.applicationInfo = actInfo.applicationInfo;
			origActInfo.configChanges = actInfo.configChanges;
			origActInfo.descriptionRes = actInfo.descriptionRes;
			origActInfo.enabled = actInfo.enabled;
			origActInfo.exported = actInfo.exported;
			origActInfo.flags = actInfo.flags;
			origActInfo.icon = actInfo.icon;
			origActInfo.labelRes = actInfo.labelRes;
			origActInfo.logo = actInfo.logo;
			origActInfo.metaData = actInfo.metaData;
			origActInfo.name = actInfo.name;
			origActInfo.nonLocalizedLabel = actInfo.nonLocalizedLabel;
			origActInfo.packageName = actInfo.packageName;
			origActInfo.permission = actInfo.permission;
			// origActInfo.processName
			origActInfo.screenOrientation = actInfo.screenOrientation;
			origActInfo.softInputMode = actInfo.softInputMode;
			origActInfo.targetActivity = actInfo.targetActivity;
			origActInfo.taskAffinity = actInfo.taskAffinity;
			origActInfo.theme = actInfo.theme;
		}
		Log.i(tag, "changeActivityInfo->changeTheme: "+" theme = "+actInfo.getThemeResource()
				+", icon = "+actInfo.getIconResource()+", logo = "+actInfo.logo);
	}
	
	public static int getPlugActivityTheme(Activity fromAct,String pluginId) {
		PluginManager con = PluginManager.getInstance();
		PlugInfo plugin = con.getPluginById(pluginId);
		String actName = fromAct.getClass().getSuperclass().getName();
		ActivityInfo actInfo = plugin.findActivityByClassName(actName);
		int rs =  actInfo.getThemeResource();
		Log.d(tag, "getPlugActivityTheme: theme="+rs+", actName="+actName);
		changeActivityInfo(fromAct);
		return rs;
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
		Log.d(tag, "callback_onCreate(act="+fromAct.getClass().getSuperclass().getName()+", window="+fromAct.getWindow()
				+ ")");
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
		{

			String actName = fromAct.getClass().getSuperclass().getName();
			ActivityInfo actInfo = plugin.findActivityByClassName(actName);
			int resTheme = actInfo.getThemeResource();
			if (resTheme != 0) {
				boolean hasNotSetTheme = true;
				try {
					Field mTheme = ContextThemeWrapper.class
							.getDeclaredField("mTheme");
					mTheme.setAccessible(true);
					hasNotSetTheme = mTheme.get(fromAct) == null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (hasNotSetTheme) {
					changeActivityInfo(fromAct);
					fromAct.setTheme(resTheme);
				}
			}
		}
		// 如果是三星Galaxy S4 手机，则使用包装的LayoutInflater替换原LayoutInflater
		// 这款手机在解析内置的布局文件时有各种错误
//		if (android.os.Build.MODEL.equals("GT-I9500")) {
//			Window window = fromAct.getWindow();// 得到 PhoneWindow 实例
//			try {
//				Object origInf = ReflectionUtils.getFieldValue(window, "mLayoutInflater");
//				if(!(origInf instanceof LayoutInflaterWrapper)){
//					ReflectionUtils.setFieldValue(window, "mLayoutInflater",
//							new LayoutInflaterWrapper(window.getLayoutInflater()));
//				}
//			} catch (Exception e) {
//				Log.e(tag, Log.getStackTraceString(e));
//			}
//		}
		// invoke callback
		PluginActivityLifeCycleCallback callback = con
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onCreate(pluginId, fromAct);
		}
	}

	public static void callback_onResume(String pluginId, Activity fromAct) {
		Log.d(tag, "callback_onResume(act="+fromAct.getClass().getSuperclass().getName()+")");
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
