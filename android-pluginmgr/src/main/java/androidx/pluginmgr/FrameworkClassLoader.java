package androidx.pluginmgr;

import dalvik.system.PathClassLoader;
import android.util.Log;

/**
 * 框架类加载器（Application 的 classLoder被替换成此类的实例）
 * 
 * @author HouKangxi
 *
 */
class FrameworkClassLoader extends ClassLoader {
	private volatile String[] plugIdAndActname;

	public FrameworkClassLoader(ClassLoader parent) {
		super(parent);
	}

	String newActivityClassName(String plugId, String actName) {
		plugIdAndActname = new String[] { plugId, actName };
		return ActivityOverider.targetClassName;
	}

	protected Class<?> loadClass(String className, boolean resolv)
			throws ClassNotFoundException {
		Log.i("cl", "loadClass: " + className);
		String[] plugIdAndActname = this.plugIdAndActname;
		Log.i("cl", "plugIdAndActname = " + java.util.Arrays.toString(plugIdAndActname));
		if (plugIdAndActname != null) {
			String pluginId = plugIdAndActname[0];
			
			PlugInfo plugin = PluginManager.getInstance().getPluginById(
					pluginId);
			Log.i("cl", "plugin = " + plugin);
			if (plugin != null) {
				try {
					if (className.equals(ActivityOverider.targetClassName)) {
						String actClassName = plugIdAndActname[1];
						return plugin.getClassLoader().loadActivityClass(
								actClassName);
					}else{
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