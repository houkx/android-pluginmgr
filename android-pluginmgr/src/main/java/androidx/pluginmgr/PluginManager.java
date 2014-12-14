/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Looper;
import android.util.Log;

/**
 * 插件管理器
 * 
 * @author HouKangxi
 */
public class PluginManager implements FileFilter {
	private static final PluginManager instance = new PluginManager();
    private Activity actFrom;
	private PluginManager() {
	}

	public static PluginManager getInstance(Context context) {
		if (instance.hasInit || context == null) {
			return instance;
		}
		Context ctx = context;
		if (context instanceof Activity) {
			instance.actFrom = (Activity)context;
			ctx = ((Activity) context).getApplication();
		} else if (context instanceof Service) {
			ctx = ((Service) context).getApplication();
		} else if (context instanceof Application) {
			ctx = (Application) context;
		} else {
			ctx = context.getApplicationContext();
		}
		synchronized (PluginManager.class) {
			instance.init(ctx);
		}
		return instance;
	}

	static PluginManager getInstance() {
		return instance;
	}
	
	public boolean startMainActivity(Context context, String pkgOrId) {
		Log.d(tag, "startMainActivity by:"+pkgOrId);
		PlugInfo plug = preparePlugForStartActivity(context, pkgOrId);
		if (frameworkClassLoader == null) {
			Log.e(tag, "startMainActivity: frameworkClassLoader == null!");
			return false;
		}
		if(plug.getMainActivity()==null){
			Log.e(tag, "startMainActivity: plug.getMainActivity() == null!");
			return false;
		}
		if(plug.getMainActivity().activityInfo==null){
			Log.e(tag, "startMainActivity: plug.getMainActivity().activityInfo == null!");
			return false;
		}
		String className = frameworkClassLoader.newActivityClassName(
				plug.getId(), plug.getMainActivity().activityInfo.name);
		context.startActivity(new Intent().setComponent(new ComponentName(context, className)));
		return true;
	}
	
	public void startActivity(Context context, Intent intent) {
		performStartActivity(context, intent);
		context.startActivity(intent);
	}
	
	public void startActivityForResult(Activity activity, Intent intent,int requestCode) {
		performStartActivity(context, intent);
		activity.startActivityForResult(intent,requestCode);
	}
	
	private PlugInfo preparePlugForStartActivity(Context context, String plugIdOrPkg){
		PlugInfo plug = null;
		plug = getPluginByPackageName(plugIdOrPkg);
		if (plug == null) {
			plug = getPluginById(plugIdOrPkg);
		}
		if (plug == null) {
			throw new IllegalArgumentException("plug not found by:"
					+ plugIdOrPkg);
		}
		return plug;
	}
	
	private void performStartActivity(Context context, Intent intent) {
		checkInit();

		String plugIdOrPkg;
		String actName;
		ComponentName origComp = intent.getComponent();
		if (origComp != null) {
			plugIdOrPkg = origComp.getPackageName();
			actName = origComp.getClassName();
		} else {
			throw new IllegalArgumentException(
					"plug intent must set the ComponentName!");
		}
		PlugInfo plug = preparePlugForStartActivity(context, plugIdOrPkg);
		String className = frameworkClassLoader.newActivityClassName(
				plug.getId(), actName);
		ComponentName comp = new ComponentName(context, className);
		intent.setAction(null);
		intent.setComponent(comp);
	}
	

	private final Map<String, PlugInfo> pluginIdToInfoMap = new ConcurrentHashMap<String, PlugInfo>();
	private final Map<String, PlugInfo> pluginPkgToInfoMap = new ConcurrentHashMap<String, PlugInfo>();
	private Context context;
	private String dexOutputPath;
	private volatile boolean hasInit = false;
	private File dexInternalStoragePath;
	private FrameworkClassLoader frameworkClassLoader;
	private PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback;
	private static final String tag = "plugmgr";

	private void init(Context ctx) {
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
			frameworkClassLoader = new FrameworkClassLoader(
					ctx.getClassLoader());
			// set Application's classLoader to FrameworkClassLoader
			ReflectionUtils.setFieldValue(mPackageInfo, "mClassLoader",
					frameworkClassLoader, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		hasInit = true;
	}

	private void checkInit() {
		if (!hasInit) {
			throw new IllegalStateException("PluginManager has not init!");
		}
	}

	public PlugInfo getPluginById(String pluginId) {
		if (pluginId == null) {
			return null;
		}
		return pluginIdToInfoMap.get(pluginId);
	}

	public PlugInfo getPluginByPackageName(String packageName) {
		return pluginPkgToInfoMap.get(packageName);
	}

	public Collection<PlugInfo> getPlugins() {
		return pluginIdToInfoMap.values();
	}

	public void uninstallPluginById(String pluginId) {
		uninstallPlugin(pluginId, true);
	}

	public void uninstallPluginByPkg(String pkg) {
		uninstallPlugin(pkg, false);
	}

	private void uninstallPlugin(String k, boolean isId) {
		checkInit();
		PlugInfo pl = isId ? removePlugById(k) : removePlugByPkg(k);
		if (pl == null) {
			return;
		}
		if (context instanceof Application) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				try {
					Application.class
							.getMethod(
									"unregisterComponentCallbacks",
									Class.forName("android.content.ComponentCallbacks"))
							.invoke(context, pl.getApplication());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private PlugInfo removePlugById(String pluginId) {
		PlugInfo pl = null;
		synchronized (this) {
			pl = pluginIdToInfoMap.remove(pluginId);
			if (pl == null) {
				return null;
			}
			pluginPkgToInfoMap.remove(pl.getPackageName());
		}
		return pl;
	}

	private PlugInfo removePlugByPkg(String pkg) {
		PlugInfo pl = null;
		synchronized (this) {
			pl = pluginPkgToInfoMap.remove(pkg);
			if (pl == null) {
				return null;
			}
			pluginIdToInfoMap.remove(pl.getId());
		}
		return pl;
	}

	/**
	 * 加载指定目录下的所有插件
	 * <p>
	 * 都使用文件名作为Id
	 * 
	 * @param pluginSrcDirFile
	 *            - apk目录
	 * @return 插件数目
	 * @throws Exception
	 */
	public Collection<PlugInfo> loadPlugin(final File pluginSrcDirFile)
			throws Exception {
		checkInit();
		if (pluginSrcDirFile == null || !pluginSrcDirFile.exists()) {
			Log.e(tag, "invalidate pluginDir :"+pluginSrcDirFile);
			return null;
		}
		if (pluginSrcDirFile.isFile()) {
			ArrayList<PlugInfo> list = new ArrayList<PlugInfo>(1);
			PlugInfo one = loadPluginWithId(pluginSrcDirFile, null, null);
			list.add(one);
			return list;
		}
		// clear all first
		synchronized (this) {
			pluginPkgToInfoMap.clear();
			pluginIdToInfoMap.clear();
		}
		File[] pluginApks = pluginSrcDirFile.listFiles(this);
		if (pluginApks == null || pluginApks.length < 1) {
			throw new FileNotFoundException("could not find plugins in:"
					+ pluginSrcDirFile);
		}
		for (File pluginApk : pluginApks) {
			PlugInfo plugInfo = buildPlugInfo(pluginApk, null, null);
			if (plugInfo != null) {
				savePluginToMap(plugInfo);
			}
		}
		return pluginIdToInfoMap.values();
	}

	private synchronized void savePluginToMap(PlugInfo plugInfo) {
		pluginPkgToInfoMap.put(plugInfo.getPackageName(), plugInfo);
		pluginIdToInfoMap.put(plugInfo.getId(), plugInfo);
	}

//	/**
//	 * 单独加载一个apk <br/>
//	 * 使用文件名作为插件id <br/>
//	 * 目标文件也是与源文件同名
//	 * 
//	 * @param pluginApk
//	 * @return
//	 * @throws Exception
//	 */
//	public PlugInfo loadPlugin(File pluginApk) throws Exception {
//		return loadPluginWithId(pluginApk, null, null);
//	}

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
		checkInit();
		PlugInfo plugInfo = buildPlugInfo(pluginApk, pluginId, targetFileName);
		if (plugInfo != null) {
			savePluginToMap(plugInfo);
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
		PluginManifestUtil.setManifestInfo(context, dexPath, info);

		PluginClassLoader loader = new PluginClassLoader(dexPath,
				dexOutputPath, frameworkClassLoader, info);
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
		Log.i(tag, "buildPlugInfo: " + info);
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

	private void initPluginApplication(final PlugInfo info) throws Exception {
		String className = info.getPackageInfo().applicationInfo.name;
		Log.d(tag, info.getId() + ", ApplicationClassName = " + className);
		// create Application instance for plugin
		if (className == null) {
			Application application = new Application();
			setApplicationBase(info, application);
		} else {
			ClassLoader loader = info.getClassLoader();
			final Class<?> applicationClass = loader.loadClass(className);
			if (actFrom != null) {
				actFrom.runOnUiThread(new Runnable() {
					public void run() {
						try {
							Application application = (Application) applicationClass
									.newInstance();
							setApplicationBase(info, application);
							// invoke plugin application's onCreate()
							application.onCreate();
						} catch (Throwable e) {
							Log.e(tag, Log.getStackTraceString(e));
						}
					}
				});

			} else {
				Application application = (Application) applicationClass
						.newInstance();
				setApplicationBase(info, application);
			}
		}
	}
    private void setApplicationBase(PlugInfo info,Application application)throws Exception{
    	info.setApplication(application);
		//
		PluginContextWrapper ctxWrapper = new PluginContextWrapper(context,
				info);
		// set field: mBase
		ReflectionUtils.setFieldValue(application, "mBase", ctxWrapper);
		// set field: mLoadedApk, get from context(framework application)
		Object mLoadedApk = ReflectionUtils
				.getFieldValue(context, "mLoadedApk");
		ReflectionUtils.setFieldValue(application, "mLoadedApk", mLoadedApk);
		if (context instanceof Application) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				Application.class.getMethod("registerComponentCallbacks",
						Class.forName("android.content.ComponentCallbacks"))
						.invoke(context, application);
			}
		}
    }
	private void copyApkToPrivatePath(File pluginApk, File f) {
		// if (f.exists() && pluginApk.length() == f.length()) {
		// // 这里只是简单的判断如果两个文件长度相同则不拷贝，严格的做应该比较签名如 md5\sha-1
		// return;
		// }
		FileUtil.copyFile(pluginApk, f);
	}

	public File getDexInternalStoragePath() {
		return dexInternalStoragePath;
	}

	public Context getContext() {
		return context;
	}

	public PluginActivityLifeCycleCallback getPluginActivityLifeCycleCallback() {
		return pluginActivityLifeCycleCallback;
	}

	public void setPluginActivityLifeCycleCallback(
			PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback) {
		this.pluginActivityLifeCycleCallback = pluginActivityLifeCycleCallback;
	}

	FrameworkClassLoader getFrameworkClassLoader() {
		return frameworkClassLoader;
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
