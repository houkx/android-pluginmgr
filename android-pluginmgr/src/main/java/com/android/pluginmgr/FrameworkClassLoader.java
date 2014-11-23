package com.android.pluginmgr;


/**
 * 框架类加载器（Application 的 classLoder被替换成此类的实例）
 * 
 * @author HouKangxi
 *
 */
class FrameworkClassLoader extends ClassLoader {

	public FrameworkClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		PlugInfo plugin = PluginContainer.getInstance().getCurrentPlugin();
		if (plugin != null && plugin.getClassLoader() != null) {
			// Log.i(tag + ":MyClassLoader", "loadClass( " + className +
			// " )");
			try {
				Class<?> c = plugin.getClassLoader().loadClass(className);
				if (c != null)
					return c;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				Class<?> c = getSystemClassLoader().loadClass(className);
				if (c != null)
					return c;
				else
					return super.loadClass(className);
			}
		} 
		return super.loadClass(className);
	}
}