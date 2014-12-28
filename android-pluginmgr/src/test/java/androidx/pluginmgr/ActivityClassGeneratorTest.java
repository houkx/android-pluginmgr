/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;
import java.util.List;

import org.junit.Test;

import android.app.Activity;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

/**
 * 
 * @author HouKangxi
 *
 */
public class ActivityClassGeneratorTest {

//	@Test
	public void test(){
		XmlManifestReader.getManifestXMLFromAPK("D://cn/LQLauncher_2.1_ZTE_Launcher_android-19_2014-12-15_18-12-26_87-release.apk");
	}

//	@Test
	public void testGenerateActivity_method_startService() throws Throwable {
		String superClassName = "androidplugdemo.SthActivity";
		String targetClassName = ActivityOverider.targetClassName;
		String pluginId = "activityTest_v1";
		String pkgName = "androidplugdemo";
		File saveTo = new File(System.getProperty("user.dir")
				+ "/target/StartTestActivity.dex");
		ActivityClassGenerator.createActivityDex(superClassName,
				targetClassName, saveTo, pluginId, pkgName);
	}
}
