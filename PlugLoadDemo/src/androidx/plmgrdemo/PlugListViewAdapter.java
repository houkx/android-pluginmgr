package androidx.plmgrdemo;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.pluginmgr.PlugInfo;

class PlugListViewAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<PlugInfo> datas;
	
	public PlugListViewAdapter(Context context, Collection<PlugInfo> datas) {
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

	private Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
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
		String title = plug.getResources().getString(plug.getPackageInfo().applicationInfo.labelRes);
		mViewHolder.title.setText(title);
		SoftReference<Drawable> imgref = imageCache.get(plug.getId());
		Drawable drawable;
		if (imgref != null) {
			drawable = imgref.get();
		} else {
			drawable = plug.getResources().getDrawable(
					plug.getPackageInfo().applicationInfo.icon);
			imageCache.put(plug.getId(), new SoftReference<Drawable>(drawable));
		}
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