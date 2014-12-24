/**
 * 
 */
package androidx.pluginmgr;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author HouKangxi
 *
 */
public class PluginPackageManager extends PackageManager {
    private PackageManager wrapper;
    
	public PluginPackageManager(PackageManager wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public PackageInfo getPackageInfo(String packageName, int flags)
			throws NameNotFoundException {
		PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
		if (plugin != null) {
			return plugin.getPackageInfo();
		}
		return wrapper.getPackageInfo(packageName, flags);
	}

	@Override
	public String[] currentToCanonicalPackageNames(String[] names) {
		return wrapper.currentToCanonicalPackageNames(names);
	}

	@Override
	public String[] canonicalToCurrentPackageNames(String[] names) {
		return wrapper.canonicalToCurrentPackageNames(names);
	}

	@Override
	public Intent getLaunchIntentForPackage(String packageName) {
//		PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
//		if (plugin != null) {
//			//TODO
//		}
		return wrapper.getLaunchIntentForPackage(packageName);
	}

	@Override
	public int[] getPackageGids(String packageName)
			throws NameNotFoundException {
		PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
		if (plugin != null) {
			return plugin.getPackageInfo().gids;
		}
		return wrapper.getPackageGids(packageName);
	}

	@Override
	public PermissionInfo getPermissionInfo(String name, int flags)
			throws NameNotFoundException {
		//TODO
		return wrapper.getPermissionInfo(name, flags);
	}

	@Override
	public List<PermissionInfo> queryPermissionsByGroup(String group, int flags)
			throws NameNotFoundException {
		// TODO 
		return wrapper.queryPermissionsByGroup(group, flags);
	}

	@Override
	public PermissionGroupInfo getPermissionGroupInfo(String name, int flags)
			throws NameNotFoundException {
		// TODO 
		return wrapper.getPermissionGroupInfo(name, flags);
	}

	@Override
	public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
		// TODO 
		return wrapper.getAllPermissionGroups(flags);
	}

	@Override
	public ApplicationInfo getApplicationInfo(String packageName, int flags)
			throws NameNotFoundException {
		PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
		if (plugin != null) {
			return plugin.getPackageInfo().applicationInfo;
		}
		return wrapper.getApplicationInfo(packageName, flags);
	}

	@Override
	public ActivityInfo getActivityInfo(ComponentName component, int flags)
			throws NameNotFoundException {
		String packageName=component.getPackageName();
		if (packageName != null) {
			PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
			if (plugin != null) {
				String actClassName = component.getClassName();
				if (actClassName != null) {
					return plugin.findActivityByClassName(actClassName);
				}
			}
		}
		return wrapper.getActivityInfo(component, flags);
	}

	@Override
	public ActivityInfo getReceiverInfo(ComponentName component, int flags)
			throws NameNotFoundException {
		String packageName=component.getPackageName();
		if (packageName != null) {
			PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
			if (plugin != null) {
				String className = component.getClassName();
				if (className != null) {
					return plugin.findReceiverByClassName(className);
				}
			}
		} 
		return wrapper.getReceiverInfo(component, flags);
	}

	@Override
	public ServiceInfo getServiceInfo(ComponentName component, int flags)
			throws NameNotFoundException {
		String packageName=component.getPackageName();
		if (packageName != null) {
			PlugInfo plugin = PluginManager.getInstance().getPluginByPackageName(packageName);
			if (plugin != null) {
				String className = component.getClassName();
				if (className != null) {
					return plugin.findServiceByClassName(className);
				}
			}
		} 
		return wrapper.getServiceInfo(component, flags);
	}

	@Override
	public ProviderInfo getProviderInfo(ComponentName component, int flags)
			throws NameNotFoundException {
		// TODO getProviderInfo
		return wrapper.getProviderInfo(component, flags);
	}

	@Override
	public List<PackageInfo> getInstalledPackages(int flags) {
		return wrapper.getInstalledPackages(flags);
	}

	@Override
	public int checkPermission(String permName, String pkgName) {
		// TODO checkPermission
		return wrapper.checkPermission(permName, pkgName);
	}

	@Override
	public boolean addPermission(PermissionInfo info) {
		// TODO addPermission
		return wrapper.addPermission(info);
	}

	@Override
	public boolean addPermissionAsync(PermissionInfo info) {
		// TODO addPermissionAsync 
		return wrapper.addPermission(info);
	}

	@Override
	public void removePermission(String name) {
		// TODO removePermission
		wrapper.removePermission(name);
	}

	@Override
	public int checkSignatures(String pkg1, String pkg2) {
		// TODO checkSignatures
		return wrapper.checkSignatures(pkg1, pkg2); 
	}

	@Override
	public int checkSignatures(int uid1, int uid2) {
		// TODO checkSignatures
		return wrapper.checkSignatures(uid1, uid2); 
	}

	@Override
	public String[] getPackagesForUid(int uid) {
		// TODO getPackagesForUid
		return wrapper.getPackagesForUid(uid);
	}

	@Override
	public String getNameForUid(int uid) {
		// TODO getNameForUid
		return wrapper.getNameForUid(uid);
	}

	@Override
	public List<ApplicationInfo> getInstalledApplications(int flags) {
		return wrapper.getInstalledApplications(flags);
	}

	@Override
	public String[] getSystemSharedLibraryNames() {
		// TODO getSystemSharedLibraryNames
		return wrapper.getSystemSharedLibraryNames();
	}

	@Override
	public FeatureInfo[] getSystemAvailableFeatures() {
		// TODO getSystemAvailableFeatures
		return wrapper.getSystemAvailableFeatures();
	}

	@Override
	public boolean hasSystemFeature(String name) {
		// TODO hasSystemFeature
		return wrapper.hasSystemFeature(name);
	}

	@Override
	public ResolveInfo resolveActivity(Intent intent, int flags) {
		// TODO resolveActivity
//		ComponentName compname = intent.getComponent();
//		if(compname!=null){
//			String packageName = compname.getPackageName();
//			if(packageName!=null){
//				PlugInfo plug = PluginManager.getInstance().getPluginByPackageName(packageName);
//				if (plug != null) {
//					String className = compname.getClassName();
//					if(className!=null){
//						return plug.resolveActivity()
//					}
//				}
//			}
//		}
		return wrapper.resolveActivity(intent, flags);
	}

	@Override
	public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryIntentActivities(intent, flags);
	}

	@Override
	public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller,
			Intent[] specifics, Intent intent, int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryIntentActivityOptions(caller, specifics, intent, flags);
	}

	@Override
	public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryBroadcastReceivers(intent, flags);
	}

	@Override
	public ResolveInfo resolveService(Intent intent, int flags) {
		// TODO Auto-generated method stub
		return wrapper.resolveService(intent, flags);
	}

	@Override
	public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryIntentServices(intent, flags);
	}

	@Override
	public ProviderInfo resolveContentProvider(String name, int flags) {
		// TODO Auto-generated method stub
		return wrapper.resolveContentProvider(name, flags);
	}

	@Override
	public List<ProviderInfo> queryContentProviders(String processName,
			int uid, int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryContentProviders(processName, uid, flags);
	}

	@Override
	public InstrumentationInfo getInstrumentationInfo(ComponentName className,
			int flags) throws NameNotFoundException {
		// TODO Auto-generated method stub
		return wrapper.getInstrumentationInfo(className, flags);
	}

	@Override
	public List<InstrumentationInfo> queryInstrumentation(String targetPackage,
			int flags) {
		// TODO Auto-generated method stub
		return wrapper.queryInstrumentation(targetPackage, flags);
	}

	@Override
	public Drawable getDrawable(String packageName, int resid,
			ApplicationInfo appInfo) {
		// TODO getDrawable
		return wrapper.getDrawable(packageName, resid, appInfo);
	}

	@Override
	public Drawable getActivityIcon(ComponentName activityName)
			throws NameNotFoundException {
		// TODO getActivityIcon
		PlugInfo plug = PluginManager.getInstance().getPluginByPackageName(activityName.getPackageName());
		if(plug!=null){
			ActivityInfo actInfo =plug.findActivityByClassName(activityName.getClassName());
			int icon = actInfo.icon;
			if (icon != 0) {
				return plug.getResources().getDrawable(icon);
			}
		}
		return wrapper.getActivityIcon(activityName);
	}

	@Override
	public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
		// TODO getActivityIcon(Intent intent)
		return wrapper.getActivityIcon(intent);
	}

	@Override
	public Drawable getDefaultActivityIcon() {
		// TODO getDefaultActivityIcon
		return wrapper.getDefaultActivityIcon();
	}

	@Override
	public Drawable getApplicationIcon(ApplicationInfo info) {
		// TODO getApplicationIcon
		return wrapper.getApplicationIcon(info); 
	}

	@Override
	public Drawable getApplicationIcon(String packageName)
			throws NameNotFoundException {
		// TODO getApplicationIcon
		PlugInfo plug = PluginManager.getInstance().getPluginByPackageName(packageName);
		if (plug != null) {
			int appIcon = plug.getPackageInfo().applicationInfo.icon;
			if (appIcon != 0) {
				return plug.getResources().getDrawable(appIcon);
			} else {
				return null;
			}
		}
		return wrapper.getApplicationIcon(packageName); 
	}

	@Override
	public Drawable getActivityLogo(ComponentName activityName)
			throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getActivityLogo(android.content.Intent)
	 */
	@Override
	public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getApplicationLogo(android.content.pm.ApplicationInfo)
	 */
	@Override
	public Drawable getApplicationLogo(ApplicationInfo info) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getApplicationLogo(java.lang.String)
	 */
	@Override
	public Drawable getApplicationLogo(String packageName)
			throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getText(java.lang.String, int, android.content.pm.ApplicationInfo)
	 */
	@Override
	public CharSequence getText(String packageName, int resid,
			ApplicationInfo appInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getXml(java.lang.String, int, android.content.pm.ApplicationInfo)
	 */
	@Override
	public XmlResourceParser getXml(String packageName, int resid,
			ApplicationInfo appInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getApplicationLabel(android.content.pm.ApplicationInfo)
	 */
	@Override
	public CharSequence getApplicationLabel(ApplicationInfo info) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getResourcesForActivity(android.content.ComponentName)
	 */
	@Override
	public Resources getResourcesForActivity(ComponentName activityName)
			throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getResourcesForApplication(android.content.pm.ApplicationInfo)
	 */
	@Override
	public Resources getResourcesForApplication(ApplicationInfo app)
			throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getResourcesForApplication(java.lang.String)
	 */
	@Override
	public Resources getResourcesForApplication(String appPackageName)
			throws NameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getInstallerPackageName(java.lang.String)
	 */
	@Override
	public String getInstallerPackageName(String packageName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#addPackageToPreferred(java.lang.String)
	 */
	@Override
	public void addPackageToPreferred(String packageName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#removePackageFromPreferred(java.lang.String)
	 */
	@Override
	public void removePackageFromPreferred(String packageName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getPreferredPackages(int)
	 */
	@Override
	public List<PackageInfo> getPreferredPackages(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#addPreferredActivity(android.content.IntentFilter, int, android.content.ComponentName[], android.content.ComponentName)
	 */
	@Override
	public void addPreferredActivity(IntentFilter filter, int match,
			ComponentName[] set, ComponentName activity) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#clearPackagePreferredActivities(java.lang.String)
	 */
	@Override
	public void clearPackagePreferredActivities(String packageName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getPreferredActivities(java.util.List, java.util.List, java.lang.String)
	 */
	@Override
	public int getPreferredActivities(List<IntentFilter> outFilters,
			List<ComponentName> outActivities, String packageName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#setComponentEnabledSetting(android.content.ComponentName, int, int)
	 */
	@Override
	public void setComponentEnabledSetting(ComponentName componentName,
			int newState, int flags) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getComponentEnabledSetting(android.content.ComponentName)
	 */
	@Override
	public int getComponentEnabledSetting(ComponentName componentName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#setApplicationEnabledSetting(java.lang.String, int, int)
	 */
	@Override
	public void setApplicationEnabledSetting(String packageName, int newState,
			int flags) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#getApplicationEnabledSetting(java.lang.String)
	 */
	@Override
	public int getApplicationEnabledSetting(String packageName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.pm.PackageManager#isSafeMode()
	 */
	@Override
	public boolean isSafeMode() {
		// TODO Auto-generated method stub
		return false;
	}

}
