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
package androidx.pluginmgr.environment;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件Bean
 * 
 * @author HouKangxi
 * @author Lody
 * 
 */
public class PlugInfo implements Serializable {

    // ================== FIELDS ==================
    private String id;
	private String filePath;
	private PackageInfo packageInfo;
	private Map<String,ResolveInfo> activities;
	private ResolveInfo mainActivity;
	private List<ResolveInfo> services;
	private List<ResolveInfo> receivers;
	private List<ResolveInfo> providers;

    private transient ClassLoader classLoader;
    private transient Application application;
	private transient AssetManager assetManager;
	private transient Resources resources;

	public String getPackageName() {
		return packageInfo.packageName;
	}


    public ActivityInfo findActivityByClassNameFromPkg(String actName) {
        if (actName.startsWith(".")) {
            actName = getPackageName() + actName;
        }
        if (packageInfo.activities == null) {
			return null;
		}
		for (ActivityInfo act : packageInfo.activities) {
           if(act.name.equals(actName)){
        	   return act;
           }
		}
		return null;
	}
	public ActivityInfo findActivityByClassName(String actName) {
		if (packageInfo.activities == null) {
			return null;
		}
        if (actName.startsWith(".")) {
            actName = getPackageName() + actName;
        }
        ResolveInfo act = activities.get(actName);
		if (act == null) {
			return null;
		}
		return act.activityInfo;
	}

	public ActivityInfo findActivityByAction(String action) {
		if (activities == null || activities.isEmpty()) {
			return null;
		}

        for (ResolveInfo act : activities.values()) {
			if (act.filter != null && act.filter.hasAction(action)) {
				return act.activityInfo;
			}
		}
		return null;
	}

	public ActivityInfo findReceiverByClassName(String className) {
		if (packageInfo.receivers == null) {
			return null;
		}
		for (ActivityInfo receiver : packageInfo.receivers) {
			if (receiver.name.equals(className)) {
				return receiver;
			}
		}
		return null;

	}
	public ServiceInfo findServiceByClassName(String className) {
		if (packageInfo.services == null) {
			return null;
		}
		for (ServiceInfo service : packageInfo.services) {
			if (service.name.equals(className)) {
				return service;
			}
		}
		return null;
		
	}
	public ServiceInfo findServiceByAction(String action) {
		if (services == null || services.isEmpty()) {
			return null;
		}
		for (ResolveInfo ser : services) {
			if (ser.filter != null && ser.filter.hasAction(action)) {
				return ser.serviceInfo;
			}
		}
		return null;
	}
	public void addActivity(ResolveInfo activity) {
		if (activities == null) {
            activities = new HashMap<String, ResolveInfo>(15);
        }
        fixActivityInfo(activity.activityInfo);
        activities.put(activity.activityInfo.name,activity);
		if (mainActivity == null && activity.filter != null
				&& activity.filter.hasAction("android.intent.action.MAIN")
				&& activity.filter.hasCategory("android.intent.category.LAUNCHER")
				) {
			mainActivity = activity;
		}
	}

    private void fixActivityInfo(ActivityInfo activityInfo) {
        if (activityInfo != null) {
            if (activityInfo.name.startsWith(".")) {
                activityInfo.name = getPackageName() + activityInfo.name;
            }
        }
    }

    public void addReceiver(ResolveInfo receiver) {
        if (receivers == null) {
			receivers = new ArrayList<ResolveInfo>();
		}
		receivers.add(receiver);
	}
	
	public void addService(ResolveInfo service) {
		if (services == null) {
			services = new ArrayList<ResolveInfo>();
		}
		services.add(service);
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
		activities = new HashMap<String, ResolveInfo>(packageInfo.activities.length);
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

    public AssetManager getAssets() {
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


	public Collection<ResolveInfo> getActivities() {
		if (activities == null) {
			return null;
		}
		return activities.values();
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
		return mainActivity;
	}

	public List<ResolveInfo> getReceivers() {
		return receivers;
	}

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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
}
