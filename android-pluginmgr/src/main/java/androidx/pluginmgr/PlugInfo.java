/**
 * 
 */
package androidx.pluginmgr;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * 插件Bean
 * 
 * @author HouKangxi
 * 
 */
public class PlugInfo {

	//
	// ================== FLELDS ==================
	private String id;
	private String filePath;
	private PackageInfo packageInfo;
	private List<ResolveInfo> activities = new ArrayList<ResolveInfo>();
	private List<ResolveInfo> mainActivities = new ArrayList<ResolveInfo>();
	private List<ResolveInfo> services;
	private List<ResolveInfo> receivers;
	private List<ResolveInfo> providers;
	//
	private transient PluginClassLoader classLoader;
	private transient Application application;
	private transient AssetManager assetManager;
	private transient Resources resources;

	//
	// private transient volatile String currentActivityClass;

	public String getPackageName() {
		return packageInfo.packageName;
	}

	// ================== FLAGS STARD ==================
	/**
	 * 按下back键时是否 finish Activity
	 */
	private static final int FLAG_FinishActivityOnbackPressed = 1;
	/**
	 * 是否调用父类的onBackPressed()方法
	 */
	private static final int FLAG_INVOKE_SUPER_ONBACKPRESSED = 2;

	// ================== FLAGS END ==================
	/**
	 * 按Back键时是否销毁Activity
	 */
	public boolean isFinishActivityOnbackPressed(ActivityInfo act) {
		if (act == null) {
			return false;
		}
		return containsFlag(getFlags(act), FLAG_FinishActivityOnbackPressed);
	}

	public boolean isInvokeSuperOnbackPressed(ActivityInfo act) {
		if (act == null) {
			return true;
		}
		return containsFlag(getFlags(act), FLAG_INVOKE_SUPER_ONBACKPRESSED);
	}

	public void setInvokeSuperOnbackPressed(ActivityInfo act,
			boolean invokeSuperOnbackPressed) {
		if (act == null) {
			return;
		}
		if (invokeSuperOnbackPressed) {
			setFlag(act, FLAG_INVOKE_SUPER_ONBACKPRESSED);
		} else {
			unsetFlag(act, FLAG_INVOKE_SUPER_ONBACKPRESSED);
		}
	}

	public void setFinishActivityOnbackPressed(ActivityInfo act,
			boolean finishOnbackPressed) {
		if (act == null) {
			return;
		}
		if (finishOnbackPressed) {
			setFlag(act, FLAG_FinishActivityOnbackPressed);
		} else {
			unsetFlag(act, FLAG_FinishActivityOnbackPressed);
		}
	}

	public ActivityInfo findActivityByClassName(String actName) {
		if (packageInfo.activities == null) {
			return null;
		}
		for (ActivityInfo act : packageInfo.activities) {
			if (act.name.equals(actName)) {
				return act;
			}
		}
		return null;

	}

	public ActivityInfo findActivityByAction(String action) {
		if (activities == null || activities.isEmpty()) {
			return null;
		}
		for (ResolveInfo act : activities) {
			if (act.filter != null && act.filter.hasAction(action)) {
				return act.activityInfo;
			}
		}
		return null;
	}

	public ActivityInfo findReceiverByClassName(String actName) {
		if (packageInfo.receivers == null) {
			return null;
		}
		for (ActivityInfo receiver : packageInfo.receivers) {
			if (receiver.name.equals(actName)) {
				return receiver;
			}
		}
		return null;

	}

	public void addActivity(ResolveInfo activity) {
		activities.add(activity);
		if (activity.filter != null
				&& activity.filter.hasAction("android.intent.action.MAIN")) {
			mainActivities.add(activity);
		}
	}

	public void addReceiver(ResolveInfo receiver) {
		if (receivers == null) {
			receivers = new ArrayList<ResolveInfo>();
		}
		receivers.add(receiver);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}

	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	public PluginClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(PluginClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	// public String getCurrentActivityClass() {
	// return currentActivityClass;
	// }
	//
	// public void setCurrentActivityClass(String currentActivityClass) {
	// this.currentActivityClass = currentActivityClass;
	// }

	public List<ResolveInfo> getActivities() {
		return activities;
	}

	public List<ResolveInfo> getMainActivities() {
		return mainActivities;
	}

	public List<ResolveInfo> getServices() {
		return services;
	}

	public void setServices(List<ResolveInfo> services) {
		this.services = services;
	}

	public List<ResolveInfo> getProviders() {
		return providers;
	}

	public void setProviders(List<ResolveInfo> providers) {
		this.providers = providers;
	}

	public ResolveInfo getMainActivity() {
		if (mainActivities == null || mainActivities.isEmpty()) {
			return null;
		}
		return mainActivities.get(0);
	}

	public List<ResolveInfo> getReceivers() {
		return receivers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlugInfo other = (PlugInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + "[ id=" + id + ", pkg=" + getPackageName()
				+ " ]";
	}

	private static synchronized int getFlags(ActivityInfo act) {
		return act.logo;
	}

	private static synchronized void setFlag(ActivityInfo act, int flag) {
		act.logo |= flag;
	}

	private static synchronized void unsetFlag(ActivityInfo act, int flag) {
		act.logo &= ~flag;
	}

	private static boolean containsFlag(int vFlags, int flag) {
		return (vFlags & flag) == flag;
	}
}
