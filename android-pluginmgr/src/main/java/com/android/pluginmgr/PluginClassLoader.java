/**
 * 
 */
package com.android.pluginmgr;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.pm.ResolveInfo;
import android.util.Log;
import dalvik.system.DexClassLoader;

/**
 * 插件类加载器
 * 
 * @author HouKangxi
 * 
 */
public class PluginClassLoader extends DexClassLoader {
	private final PlugInfo thisPlugin;
	private final String tag;
	private String optimizedDirectory;
	private String libraryPath;
	/**
	 * Activity 的类加载器
	 */
	private Map<String, ClassLoader> proxyActivityLoaderMap = new ConcurrentHashMap<String, ClassLoader>();

	public PluginClassLoader(String dexPath, String optimizedDirectory,
			ClassLoader parent, PlugInfo plugin) {
		super(dexPath, optimizedDirectory,
				plugin.getPackageInfo().applicationInfo.nativeLibraryDir,
				parent);
		thisPlugin = plugin;
		this.libraryPath = plugin.getPackageInfo().applicationInfo.nativeLibraryDir;
		this.optimizedDirectory = optimizedDirectory;
		tag = "PluginClassLoader( " + plugin.getPackageInfo().packageName
				+ " )";
	}

	@Override
	public Class<?> loadClass(final String className)
			throws ClassNotFoundException {
		// Log.d(tag, "loadClass: " + className);
		//
		PluginContainer con = PluginContainer.getInstance();
		PlugInfo currentPlugin = con.getCurrentPlugin();
		// Log.i(tag, "currentPlugin == " + currentPlugin);
		// 加载Activity的情况
		if (className.equals(ActivityOverider.targetClassName)
				&& currentPlugin != null) {
			//
			String currentActivity = currentPlugin.getCurrentActivityClass();
			// Log.i(tag, "currentActivity == " + currentActivity);
			if (currentActivity == null) {
				Log.e(tag, "当前Activity is null");
				ResolveInfo mainAct = currentPlugin.getMainActivity();
				if (mainAct != null) {
					currentActivity = mainAct.activityInfo.name;
				}
			}
			if (currentActivity == null) {
				return super.loadClass(className);
			}
			File dexSaveDir = ActivityOverider.getPorxyActivityDexPath(
					currentPlugin, currentActivity);
			// 在类加载之前检查创建代理的Activity dex文件，以免调用者忘记生成此文件
			ActivityOverider.createProxyDex(currentPlugin, currentActivity,
					dexSaveDir, true);

			// 绝大多数情况下，目标插件currentPlugin等于
			// thisPlugin,除非thisPlugin要跳到别的插件的activity
			if (currentPlugin.equals(thisPlugin)) {
				ClassLoader lc = proxyActivityLoaderMap.get(currentActivity);
				if (lc == null) {
					String dexPath = dexSaveDir.getAbsolutePath();
					// Log.i(tag + ":dexPath=", dexPath);
					// 如果activity 调用了本地方法，那肯定是插件的本地库路径
					// 所以 libraryPath 使用构造方法传入的参数
					lc = new DexClassLoader(dexPath, optimizedDirectory,
							libraryPath, this);
					proxyActivityLoaderMap.put(currentActivity, lc);
				}
				// Log.i(tag, "proxyActivityLoader.loadClass: " + className);
				return lc.loadClass(className);
			} else {
				return currentPlugin.getClassLoader().loadClass(className);
			}
		}
		// ------------------------ 一般情况 -------------------------------
		//
		// 优先在当前插件中查找类，一般情况下都可以找到，因为插件可以独立运行
		// 但是如果插件和框架中都有相同全类名的类，则需要插件中删掉这些类
		//
		// 如果很可能是android framework 层的类，则由传统方式加载
		// if (className.startsWith("android.") ||
		// className.startsWith("java.")) {
		// return super.loadClass(className);
		// }
		// // 此类加载策略打破了传统的双亲委派模型
		// // 先自己找，找不到的话再委托给父的
		// try {
		// Class<?> clazz = findClass(className);
		// if (clazz != null) {
		// return clazz;
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// 否则使用父的（也就是框架的）类加载加载
		return super.loadClass(className);
	}
}
