/**
 * 
 */
package com.android.pluginmgr;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * 插件容器
 * 
 * @author HouKangxi
 */
public class PluginContainer implements FileFilter {
	private static final PluginContainer ins = new PluginContainer();

	public static PluginContainer getInstance() {
		return ins;
	}

	private PluginContainer() {
	}

	// private final Map<String, PlugInfo> pluginPackageToInfoMap = new
	// ConcurrentHashMap<String, PlugInfo>();
	private final Map<String, PlugInfo> pluginIdToInfoMap = new ConcurrentHashMap<String, PlugInfo>();
	private Application context;

	private String dexOutputPath;
	private transient boolean hasInit = false;
	/**
	 * 当前的插件Id
	 */
	private transient volatile String currentPluginId;
	private File dexInternalStoragePath;
	private PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback;

	public PlugInfo getCurrentPlugin() {
		return getPluginById(currentPluginId);
	}

	public void setCurrentPlugin(PlugInfo currentPlugin) {
		this.currentPluginId = currentPlugin == null ? null : currentPlugin
				.getId();
	}

	public synchronized void init(Application ctx) {
		hasInit = true;
		context = ctx;
		File optimizedDexPath = ctx.getDir("outdex", Context.MODE_PRIVATE);
		if (!optimizedDexPath.exists()) {
			optimizedDexPath.mkdirs();
		}
		dexOutputPath = optimizedDexPath.getAbsolutePath();
		dexInternalStoragePath = context.getDir("dex", Context.MODE_PRIVATE);
		dexInternalStoragePath.mkdirs();
		try {
			Object mPackageInfo = ReflectionUtils.getFieldValue(ctx,
					"mBase.mPackageInfo", true);
			ClassLoader mClassLoader = ctx.getClassLoader();
			FrameworkClassLoader cl = new FrameworkClassLoader(mClassLoader);
			ReflectionUtils.setFieldValue(mPackageInfo, "mClassLoader", cl,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkInit() {
		if (!hasInit) {
			throw new IllegalStateException(
					"please call init(Context ctx) first!");
		}
	}

	public PlugInfo getPluginById(String pluginId) {
		if (pluginId == null) {
			return null;
		}
		return pluginIdToInfoMap.get(pluginId);
	}

	public PlugInfo getPluginByPackageName(String packageName) {
		for (PlugInfo pl : pluginIdToInfoMap.values()) {
			if (pl.getPackageName().equals(packageName)) {
				return pl;
			}
		}
		return null;
	}

	public Collection<PlugInfo> getPlugins() {
		return pluginIdToInfoMap.values();
	}

	public void uninstallPlugin(String pluginId) {
		checkInit();
		PlugInfo pl = pluginIdToInfoMap.remove(pluginId);
		if (pl == null) {
			return;
		}
		// pluginPackageToInfoMap.remove(pl.getPackageInfo().packageName);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
		    try {
				Application.class.getMethod("unregisterComponentCallbacks", Application.class)
				.invoke(context, pl.getApplication());
			}  catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 加载指定目录下的所有插件
	 * 
	 * @param pluginSrcDirFile
	 *            - apk目录
	 * @return 插件数目
	 * @throws Exception
	 */
	public int loadAllPluginsFromDirectory(File pluginSrcDirFile)
			throws Exception {
		checkInit();
		// pluginPackageToInfoMap.clear();
		pluginIdToInfoMap.clear();
		if (!pluginSrcDirFile.exists()) {
			return 0;
		}
		File[] pluginApks = pluginSrcDirFile.listFiles(this);
		if (pluginApks == null || pluginApks.length < 1) {
			throw new FileNotFoundException("could not find plugins in:"
					+ pluginSrcDirFile);
		}
		for (File pluginApk : pluginApks) {
			PlugInfo plugInfo = buildPlugInfo(pluginApk, null, null);
			if (plugInfo != null) {
				// pluginPackageToInfoMap.put(
				// plugInfo.getPackageInfo().packageName, plugInfo);
				pluginIdToInfoMap.put(plugInfo.getId(), plugInfo);
			}
		}
		return pluginIdToInfoMap.size();
	}

	/**
	 * 单独加载一个apk <br/>
	 * 使用文件名作为插件id <br/>
	 * 目标文件也是与源文件同名
	 * 
	 * @param pluginApk
	 * @return
	 * @throws Exception
	 */
	public PlugInfo loadPlugin(File pluginApk) throws Exception {
		return loadPluginWithId(pluginApk, null, null);
	}

	/**
	 * 单独加载一个apk
	 * 
	 * @param pluginApk
	 * @param pluginId
	 *            - 如果参数为null,则使用文件名作为插件id
	 * @return
	 * @throws Exception
	 */
	public PlugInfo loadPluginWithId(File pluginApk, String pluginId)
			throws Exception {
		return loadPluginWithId(pluginApk, pluginId, null);
	}

	public PlugInfo loadPluginWithId(File pluginApk, String pluginId,
			String targetFileName) throws Exception {
		PlugInfo plugInfo = buildPlugInfo(pluginApk, pluginId, targetFileName);
		if (plugInfo != null) {
			// pluginPackageToInfoMap.put(plugInfo.getPackageInfo().packageName,
			// plugInfo);
			pluginIdToInfoMap.put(plugInfo.getId(), plugInfo);
		}
		return plugInfo;
	}

	private PlugInfo buildPlugInfo(File pluginApk, String pluginId,
			String targetFileName) throws Exception {
		PlugInfo info = new PlugInfo();
		info.setId(pluginId == null ? pluginApk.getName() : pluginId);
		info.setFilePath(pluginApk.getAbsolutePath());

		File privateFile = new File(dexInternalStoragePath,
				targetFileName == null ? pluginApk.getName() : targetFileName);
		if (!pluginApk.getAbsolutePath().equals(privateFile.getAbsolutePath())) {
			copyApkToPrivatePath(pluginApk, privateFile);
		}
		String dexPath = privateFile.getAbsolutePath();
		final ClassLoader parentLoader = context.getClassLoader();
		ManifestReader.setManifestInfo(context, dexPath, info);

		PluginClassLoader loader = new PluginClassLoader(dexPath,
				dexOutputPath, parentLoader, info);
		info.setClassLoader(loader);

		try {
			AssetManager am = (AssetManager) AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class)
					.invoke(am, dexPath);
			info.setAssetManager(am);
			Resources ctxres = context.getResources();
			Resources res = new Resources(am, ctxres.getDisplayMetrics(),
					ctxres.getConfiguration());
			info.setResources(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		initPluginApplication(info);
		// createPluginActivityProxyDexes(info);
		return info;
	}

	// private void createPluginActivityProxyDexes(PlugInfo plugin) {
	// {
	// ActInfo act = plugin.getApplicationInfo().getMainActivity();
	// if (act != null) {
	// ActivityOverider.createProxyDex(plugin, act.name, false);
	// }
	// }
	// if (plugin.getApplicationInfo().getOtherActivities() != null) {
	// for (ActInfo act : plugin.getApplicationInfo().getOtherActivities())
	// {
	// ActivityOverider.createProxyDex(plugin, act.name, false);
	// }
	// }
	// }

	private void initPluginApplication(PlugInfo info) throws Exception {
		String className = info.getPackageInfo().applicationInfo.name;
		// create Application instance for plugin
		Application application = null;
		if (className == null) {
			application = new Application();
		} else {
			ClassLoader loader = info.getClassLoader();
			Class<?> applicationClass = loader.loadClass(className);
			application = (Application) applicationClass.newInstance();
		}
		info.setApplication(application);
		//
		PluginContextWrapper ctxWrapper = new PluginContextWrapper(context,
				info);
		// set field: mBase
		ReflectionUtils.setFieldValue(application, "mBase", ctxWrapper);
		// set ActivityManager
		// {
		// ActivityManager actmgr_orig = (ActivityManager) context
		// .getSystemService(Context.ACTIVITY_SERVICE);
		// PluginActivityManager actmgr = new PluginActivityManager(
		// actmgr_orig, info);
		// info.activityManager = actmgr;
		// }
		// set field: mLoadedApk, get from context(framework application)
		Object mLoadedApk = ReflectionUtils
				.getFieldValue(context, "mLoadedApk");
		ReflectionUtils.setFieldValue(application, "mLoadedApk", mLoadedApk);
		if (android.os.Build.VERSION.SDK_INT >= 14){
			Application.class.getMethod("registerComponentCallbacks", Application.class)
		    .invoke(context, application);
		}
		// invoke application's onCreate()
		application.onCreate();
	}

	private void copyApkToPrivatePath(File pluginApk, File f) {
		// if (f.exists() && pluginApk.length() == f.length()) {
		// // TODO 这里只是简单的判断如果两个文件长度相同则不拷贝，严格的做应该比较签名如 md5\sha-1
		// return;
		// }
		FileUtil.copyFile(pluginApk, f);
	}

	public File getDexInternalStoragePath() {
		return dexInternalStoragePath;
	}

	public Application getContext() {
		return context;
	}

	public PluginActivityLifeCycleCallback getPluginActivityLifeCycleCallback() {
		return pluginActivityLifeCycleCallback;
	}

	public void setPluginActivityLifeCycleCallback(
			PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback) {
		this.pluginActivityLifeCycleCallback = pluginActivityLifeCycleCallback;
	}

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return false;
		}
		String fname = pathname.getName();
		return fname.endsWith(".apk");
	}

}
