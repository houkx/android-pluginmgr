/**
 * 
 */
package androidx.plmgrdemo;

import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
/**
 * 
 * @author HouKangxi
 *
 */
public class ThreadLogger implements UncaughtExceptionHandler {

	/* (non-Javadoc)
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hhmmss");
		Date d=new Date();
		String path=Environment 
				.getExternalStorageDirectory().getPath()+"/PlugCrash_"+df.format(d)
						+ ".log";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(path));
			ex.printStackTrace(pw); 
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(pw!=null){
				pw.close();
			}
		}
		
	}

}
