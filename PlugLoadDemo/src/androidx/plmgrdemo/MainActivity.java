package androidx.plmgrdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Collection;

import androidx.pluginmgr.PluginManager;
import androidx.pluginmgr.environment.PlugInfo;

public class MainActivity extends Activity {
	private ListView plugListView;
	//
	private PluginManager plugMgr;

	private static final String sdcard = Environment
			.getExternalStorageDirectory().getAbsolutePath();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final EditText pluginDirTxt = (EditText) findViewById(R.id.pluginDirTxt);
		Button pluginLoader = (Button) findViewById(R.id.pluginLoader);
		plugListView = (ListView) findViewById(R.id.pluglist);

		plugMgr = PluginManager.getSingleton();

		String pluginSrcDir = sdcard;
		pluginDirTxt.setText(pluginSrcDir);

		plugListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				plugItemClick(position);
			}
		});

		final Context context = this;
		pluginLoader.setOnClickListener(new View.OnClickListener() {
			private volatile boolean plugLoading = false;

			@Override
			public void onClick(View v) {
				final String dirText = pluginDirTxt.getText().toString().trim();
				if (TextUtils.isEmpty(dirText)) {
					Toast.makeText(context, getString(R.string.pl_dir),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (plugLoading) {
					Toast.makeText(context, getString(R.string.loading),
							Toast.LENGTH_LONG).show();
					return;
				}
				String strDialogTitle = getString(R.string.dialod_loading_title);
				String strDialogBody = getString(R.string.dialod_loading_body);
				final ProgressDialog dialogLoading = ProgressDialog.show(
						context, strDialogTitle, strDialogBody, true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						plugLoading = true;
						try {
							Collection<PlugInfo> plugs = plugMgr
									.loadPlugin(new File(dirText));
                            PluginManager.getSingleton().dump();
                            setPlugins(plugs);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							dialogLoading.dismiss();
						}
						plugLoading = false;
					}
				}).start();
			}
		});
	}

	private void plugItemClick(int position) {
		PlugInfo plug = (PlugInfo) plugListView.getItemAtPosition(position);
		plugMgr.startMainActivity(this, plug);
	}

	private void setPlugins(final Collection<PlugInfo> plugs) {
		if (plugs == null || plugs.isEmpty()) {
			return;
		}
		final ListAdapter adapter = new PlugListViewAdapter(this, plugs);
		runOnUiThread(new Runnable() {
			public void run() {
				plugListView.setAdapter(adapter);
			}
		});
	}
}
