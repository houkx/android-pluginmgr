/**
 * 
 */
package com.android.pluginmgr;

import java.io.File;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
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
	public static final String targetClassName = "com.android.pluginmgr.PluginActivity";

	/**
	 * 处理 Intent 跳转
	 * <p>
	 * 供插件中的 startActivity 调用
	 * 
	 * @param intent
	 *            - 启动其他Activity的Intent请求
	 * @param requestCode
	 * @param options
	 * @param pluginId
	 *            - 插件id
	 * @param fromAct
	 *            - 发出请求的Activity
	 * @return 修改后的 Intent
	 */
	public static Intent newIntent(Intent intent, int requestCode,
			Bundle options, String pluginId, Activity fromAct) {
		// 主要做以下工作：
		// 1 、修改Intent的跳转目标
		// 2 、赋值当前的插件PluginContainer.currentPlugin，帮助插件类加载器决定使用哪个activity类加载器
		String action = intent.getAction();
		PluginContainer con = PluginContainer.getInstance();
		if (action != null) {
			// <-- 开始 处理 android.intent.action 开头的action
			if (action.startsWith("android.intent.action")) {
				// 如果想跳到MainActivity,则判断如果当前插件不是Null,则跳到当前插件的MainActivity
				if (action.endsWith(".MAIN") && con.getCurrentPlugin() != null) {
					if (intent.getComponent() != null) {
						if (intent.getPackage().equals(
								con.getCurrentPlugin().getPackageName())) {
							// TODO 插件内跳转到自己的MainActivity,
							// currentPlugin不变,这种情况比较少见，程序一般不会主动跳到自己的启动界面

						} else {
							// 插件欲跳转到其他插件的MainActivity
							PlugInfo otherPlugin = con
									.getPluginByPackageName(intent.getPackage());
							if (otherPlugin != null) {
								con.setCurrentPlugin(otherPlugin);
								ResolveInfo otherMain = otherPlugin
										.getMainActivity();
								// TODO 取到插件中相应的activity, 并产生.dex文件
								// dex 文件 路径 规范为：插件apk文件名.acts/activity类名.dex
								// 如：
								// pluginTest.apk.acts/com.example.activity.SampleActivity.dex
								createProxyDex_(otherPlugin,
										otherMain.activityInfo);
							}
						}
					} else {
						// 这种情况，不好确定此Intent想要跳到哪里
					}
					setPluginIntent(fromAct, intent);
				}
				// 系统的action, 直接返回
				return intent;
			}
			// --> 结束 处理 android.intent.action 开头的action
			//
			// <-- 开始处理普通action
			// 先判断activity所在的插件有没有对应action,因为绝大多数情况下应用都是在其内部界面之间跳转
			PlugInfo thisPlugin = con.getPluginById(pluginId);
			ActivityInfo otherAct = thisPlugin.findActivityByAction(action);
			if (otherAct != null) {
				createProxyDex_(thisPlugin, otherAct);
				con.setCurrentPlugin(thisPlugin);
				setPluginIntent(fromAct, intent);
			} else {
				for (PlugInfo plugInfo : con.getPlugins()) {
					if (plugInfo == thisPlugin) {
						continue;
					}
					ActivityInfo act = plugInfo.findActivityByAction(action);
					if (act != null) {
						createProxyDex_(plugInfo, act);
						con.setCurrentPlugin(plugInfo);
						setPluginIntent(fromAct, intent);
						break;
					}
				}
			}
		} else if (intent.getComponent() != null) {
			// action 为空，但是指定了包名和 activity类名
			ComponentName compname = intent.getComponent();
			PlugInfo thisPlugin = con.getPluginById(pluginId);
			ActivityInfo act = thisPlugin.findActivityByClassName(compname
					.getClassName());
			if (act != null) {
				createProxyDex_(thisPlugin, act);
				con.setCurrentPlugin(thisPlugin);
				setPluginIntent(fromAct, intent);
			} else {
				for (PlugInfo plugInfo : con.getPlugins()) {
					if (plugInfo == thisPlugin) {
						continue;
					}
					ActivityInfo otherAct = plugInfo
							.findActivityByClassName(compname.getClassName());
					if (otherAct != null) {
						createProxyDex_(plugInfo, otherAct);
						con.setCurrentPlugin(plugInfo);
						setPluginIntent(fromAct, intent);
						break;
					}
				}
			}
			// TODO
		}

		return intent;
	}

	private static void setPluginIntent(Context ctx, Intent intent) {
		intent.setComponent(new ComponentName(ctx.getPackageName(),
				targetClassName));
	}

	public static File getPorxyActivityDexPath(PlugInfo plugin, String activity) {
		String actName = activity;
		String pluginPath = PluginContainer.getInstance()
				.getDexInternalStoragePath().getAbsolutePath();
		String pluginDir = pluginPath + '/' + plugin.getId() + ".acts/";
		File folder = new File(pluginDir);
		folder.mkdirs();
		File saveDir = new File(pluginDir + actName + ".dex");
		return saveDir;
	}

	private static void createProxyDex_(PlugInfo plugin, ActivityInfo activity) {
		String actName = activity.name;
		plugin.setCurrentActivityClass(actName);
		createProxyDex(plugin, actName);
	}

	public static void createProxyDex(PlugInfo plugin, String activity) {
		createProxyDex(plugin, activity, true);
	}

	public static void createProxyDex(PlugInfo plugin, String activity,
			boolean lazy) {
		File saveDir = getPorxyActivityDexPath(plugin, activity);
		createProxyDex(plugin, activity, saveDir, lazy);
	}

	public static void createProxyDex(PlugInfo plugin, String activity,
			File saveDir, boolean lazy) {
		// Log.d(tag + ":createProxyDex", "plugin=" + plugin + "\n, activity="
		// + activity);
		String actName = activity;
		if (lazy && saveDir.exists()) {
			// Log.d(tag, "dex alreay exists: " + saveDir);
			// TODO 为便于测试文件替换，先这样，已经存在就不创建了，直接返回
			return;
		}
		// Log.d(tag, "actName=" + actName + ", saveDir=" + saveDir);
		try {
			String pkgName = plugin.getPackageName();
			ActivityClassGenerator.createActivityDex(actName, targetClassName,
					saveDir, plugin.getId(), pkgName);
		} catch (Throwable e) {
			Log.e(tag, Log.getStackTraceString(e));
		}
	}

	/**
	 * 按照pluginId寻找AssetManager
	 * <p>
	 * 供插件中的 onCreate()方法内 (super.onCreate()之前)调用
	 * <p>
	 * 到了这里可以说框架已经成功创建了activity <br/>
	 * TODO:<b><i> 可以在这里处理自定义Application的问题 </i></b><br/>
	 * 使用lazy-loading,lazy-init的方式 初始化插件的application <br/>
	 * 此时<code>fromAct</code>的getApplication()已经是框架的application了.<br/>
	 * 需要修改ActivityManager.RunningAppProcessInfo's field: processName <br/>
	 * 使用contextWrapper这种方式包装框架的 application 来set fromAct's Application:<br/>
	 * <code>fromAct.setApplication(wrappedApplication)</code>
	 * 
	 * @param pluginId
	 *            -插件Id
	 * @param fromAct
	 *            - 发出请求的Activity
	 * @return
	 */
	public static AssetManager getAssetManager(String pluginId, Activity fromAct) {
		PlugInfo rsinfo = PluginContainer.getInstance().getPluginById(pluginId);
		// fromAct.getApplicationContext();
		try {
			Field f = ContextWrapper.class.getDeclaredField("mBase");
			f.setAccessible(true);
			f.set(fromAct, rsinfo.getApplication());
		} catch (Exception e) {
			Log.e(tag, Log.getStackTraceString(e));
		}
		// 如果是三星Galaxy S4 手机，则使用包装的LayoutInflater替换原LayoutInflater
		// 这款手机在解析内置的布局文件时有各种错误
		if (android.os.Build.MODEL.equals("GT-I9500")) {
			Window window = fromAct.getWindow();// 得到 PhoneWindow 实例
			try {
				Field f = window.getClass().getDeclaredField("mLayoutInflater");
				f.setAccessible(true);
				f.set(window,
						new LayoutInflaterWrapper(window.getLayoutInflater()));
			} catch (Exception e) {
				Log.e(tag, Log.getStackTraceString(e));
			}
		}
		return rsinfo.getAssetManager();
	}

	/**
	 * 按下back键的方法调用
	 * 
	 * @param pluginId
	 * @param fromAct
	 * @return 是否调用父类的onBackPressed()方法
	 */
	public static boolean overideOnbackPressed(String pluginId, Activity fromAct) {
		PlugInfo plinfo = PluginContainer.getInstance().getPluginById(pluginId);
		String actName = fromAct.getClass().getSuperclass().getSimpleName();
		ActivityInfo actInfo = plinfo.findActivityByClassName(actName);
		boolean finish = plinfo.isFinishActivityOnbackPressed(actInfo);
		if (finish) {
			fromAct.finish();
		}
		boolean ivsuper = plinfo.isInvokeSuperOnbackPressed(actInfo);
		Log.i(tag, "finish? " + finish + ", ivsuper? " + ivsuper);
		return ivsuper;
	}

	//
	// =================== Activity 生命周期回调方法 ==================
	//
	public static void callback_onCreate(String pluginId, Activity fromAct) {
		PluginContainer con = PluginContainer.getInstance();
		PluginActivityLifeCycleCallback callback = con
				.getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onCreate(pluginId, fromAct);
		}
	}

	public static void callback_onResume(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onResume(pluginId, fromAct);
		}
	}

	public static void callback_onStart(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onStart(pluginId, fromAct);
		}
	}

	public static void callback_onRestart(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onRestart(pluginId, fromAct);
		}
	}

	public static void callback_onPause(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onPause(pluginId, fromAct);
		}
	}

	public static void callback_onStop(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onStop(pluginId, fromAct);
		}
	}

	public static void callback_onDestroy(String pluginId, Activity fromAct) {
		PluginActivityLifeCycleCallback callback = PluginContainer
				.getInstance().getPluginActivityLifeCycleCallback();
		if (callback != null) {
			callback.onDestroy(pluginId, fromAct);
		}
	}
}
