package androidx.plmgrdemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.pluginmgr.environment.PlugInfo;

class PlugListViewAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<PlugInfo> datas;
	private Context mContext;
	public PlugListViewAdapter(Context context, Collection<PlugInfo> datas) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		this.datas = new ArrayList<PlugInfo>(datas);
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return datas.get(position).getId().hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder = null;
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.plug_item, null);

			mViewHolder.title = (TextView) convertView.findViewById(R.id.plug_title);
			mViewHolder.icon = (ImageView) convertView
					.findViewById(R.id.plug_icon);
			mViewHolder.description = (TextView) convertView
					.findViewById(R.id.plug_description);
			mViewHolder.description.setSingleLine(true);
			mViewHolder.description.setTextSize(16.0f);
			mViewHolder.description.setEllipsize(TruncateAt.END);

			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		PlugInfo plug = datas.get(position);
		{
			int labelRes =plug.getPackageInfo().applicationInfo.labelRes;
			if (labelRes != 0) {
				String label = plug.getResources().getString(labelRes);
				mViewHolder.title.setText(label);
			} else{
				CharSequence label = plug.getPackageInfo().applicationInfo
						.loadLabel(mContext.getPackageManager());
				if (label != null) {
					mViewHolder.title.setText(label);
				}
			}
		}
		Drawable drawable = plug.getResources().getDrawable(
				plug.getPackageInfo().applicationInfo.icon);
		mViewHolder.icon.setImageDrawable(drawable);
		String descText;
		int descId = plug.getPackageInfo().applicationInfo.descriptionRes;
		if (descId == 0) {
			descText = plug.getId();
		}else{
			descText = plug.getResources().getString(descId);
		}
		mViewHolder.description.setText(descText);
		return convertView;
	}
	static class ViewHolder {
		public TextView title;
		public ImageView icon;
		public TextView description;
	}
}