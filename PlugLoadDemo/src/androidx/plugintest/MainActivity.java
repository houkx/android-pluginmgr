package androidx.plugintest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.pluginmgr.PlugInfo;
import androidx.pluginmgr.PluginManager;

public class MainActivity extends Activity implements View.OnClickListener {
	private static final String tag = "mainAct";
	private PluginManager plugMgr;
	private Collection<PlugInfo> plugins;
	EditText pathText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		plugMgr = PluginManager.getInstance(this);
		pathText = (EditText) findViewById(R.id.path_etxt);
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		pathText.setText(sdcard + "/Download/");//默认路径设为sdcard下Download
		View loadBtn = findViewById(R.id.load);
		View startBtn = findViewById(R.id.start);
		loadBtn.setTag("loadPlug");
		startBtn.setTag("startPlug");
		loadBtn.setOnClickListener(this);
		startBtn.setOnClickListener(this);
	}

	void loadPlug() {
		// load
		try {
			// 可以是包含apk的目录或单个apk文件
			File path = new File(pathText.getText().toString());
			Log.i(tag, "path=" + path + ", exists? " + path.exists());
			plugins = plugMgr.loadPlugin(path);// 加载插件apk
			if (plugins == null || plugins.isEmpty()) {
				Toast.makeText(this, "指定目录下可能没有apk!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "加载成功!", Toast.LENGTH_LONG).show();
			}
		} catch (Throwable e) {
			Toast.makeText(this, "加载失败", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	void startPlug() {
		if (plugins == null || plugins.isEmpty()) {
			Toast.makeText(this, "确定点过load按钮吗？", Toast.LENGTH_LONG).show();
			return;
		}
		// start
		try {
			// 启动第一个插件中的MainActivity
			PlugInfo firstPlug = plugins.iterator().next();
			String pkg = firstPlug.getPackageName();
			plugMgr.startMainActivity(this, pkg);
			// 或者，启动指定的activity:
			// String actName =
			// firstPlug.getActivities().get(0).activityInfo.name;
			// plugMgr.startActivity(this, new Intent().setComponent(new
			// ComponentName(pkg, actName)));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			Method m = getClass().getDeclaredMethod(v.getTag().toString());
			m.setAccessible(true);
			m.invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
