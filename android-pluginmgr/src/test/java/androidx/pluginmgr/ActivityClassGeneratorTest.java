/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * 
 * @author HouKangxi
 *
 */
public class ActivityClassGeneratorTest {

	@Test
	public void testGenerateActivity_method_startService() throws Throwable {
		String superClassName = "androidplug.servicetest.StartTestActivity";
		String targetClassName = ActivityOverider.targetClassName;
		String pluginId = "serviceTest_v1";
		String pkgName = "androidplug.servicetest";
		File saveTo = new File(System.getProperty("user.dir")
				+ "/target/ServiceStartTest.dex");
		ActivityClassGenerator.createActivityDex(superClassName,
				targetClassName, saveTo, pluginId, pkgName);
	}
}
