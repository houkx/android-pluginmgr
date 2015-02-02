/**
 * 
 */
package androidx.pluginmgr;

import java.io.File;

import org.junit.Test;

/**
 * @author Administrator
 *
 */
public class PolicyGeneratorTest {

//	@Test
	public void testGenPolicy() throws Exception {
		PolicyGenerator gen = new PolicyGenerator();
		File saveTo = new File(System.getProperty("user.dir")
				+ "/target/Policy.dex");
		gen.createDex(saveTo);
	}
}
