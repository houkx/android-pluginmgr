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
package androidx.pluginmgr;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * LayoutInflater 包装器，用来替换某些系统布局
 * <p>
 * 
 * @author HouKangxi
 *
 */
class LayoutInflaterWrapper extends LayoutInflater {
	private static final String tag = "LayoutInflaterWrapper";
	private LayoutInflater target;
	private final Class<?> layoutClass;
	private final Class<?> idClass;
	@SuppressWarnings("unused")
	private final Class<?> attrClass;
	private final int screen_title;

	public LayoutInflaterWrapper(LayoutInflater target) {
		super(target.getContext());
		this.target = target;
		Class<?> layoutClass = null;
		Class<?> idClass = null;
		Class<?> attrClass = null;
		int screen_title = 0;
		try {
			layoutClass = Class.forName("com.android.internal.R$layout");
			idClass = Class.forName("com.android.internal.R$id");
			attrClass = Class.forName("com.android.internal.R$attr");
			screen_title = layoutClass.getField("screen_title").getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.layoutClass = layoutClass;
		this.idClass = idClass;
		this.attrClass = attrClass;
		this.screen_title = screen_title;
	}

	@Override
	public LayoutInflater cloneInContext(Context newContext) {
		return target.cloneInContext(newContext);
	}

	@Override
	public Context getContext() {
		return target.getContext();
	}

	@Override
	public void setFactory(Factory factory) {
		target.setFactory(factory);
	}

	// public void setFactory2(Factory2 factory) {
	// target.setFactory2(factory);
	// }

	@Override
	public Filter getFilter() {
		return target.getFilter();
	}

	@Override
	public void setFilter(Filter filter) {
		target.setFilter(filter);
	}

	@Override
	public View inflate(int resource, ViewGroup root) {
		Log.i(tag, "inflate布局( resource=" + resource + ", root=" + root + " )");
		if (resource == screen_title) {
			Log.i(tag, "使用自定义布局");
			return createLayout_screen_simple(resource, root);
		}
		return target.inflate(resource, root);
	}

	/*
	 * private View createLayout_screen_title(int resource, ViewGroup root) {
	 * try { int layoutId = Class .forName(getContext().getPackageName() +
	 * ".R$layout") .getField("screen_title").getInt(null); Context
	 * frameworkContext = PluginContainer.getInstance() .getContext();
	 * XmlResourceParser parser = frameworkContext.getResources()
	 * .getLayout(layoutId); ViewGroup view = (ViewGroup) target.inflate(parser,
	 * root); showViews(view, 0, "根结点视图"); android.widget.ViewStub stub =
	 * (android.widget.ViewStub) view .getChildAt(0); Field field =
	 * idClass.getField("action_mode_bar_stub"); int viewStubId =
	 * field.getInt(null); stub.setId(viewStubId); return view; } catch
	 * (Exception e) { e.printStackTrace(); } return target.inflate(resource,
	 * root); }
	 * 
	 * private void showViews(View view, int n, String r) { StringBuilder sb =
	 * new StringBuilder(r); while (n-- > 0) { sb.append('='); } sb.append(' ');
	 * String msg = sb.toString() + view; Log.i(tag, msg); if (view instanceof
	 * ViewGroup) { ViewGroup g = (ViewGroup) view; for (int i = 0, len =
	 * g.getChildCount(); i < len; i++) { View c = g.getChildAt(i); showViews(c,
	 * n + 1, view.toString()); } } }
	 */
	private View createLayout_screen_simple(int resource, ViewGroup root) {
		LinearLayout lyt = new LinearLayout(getContext());
		lyt.setOrientation(LinearLayout.VERTICAL);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			try {
				LinearLayout.class.getMethod("setFitsSystemWindows",
						boolean.class).invoke(lyt, true);
			} catch (Throwable e) {
			}
		}
		int viewStubId = 0;
		int frameLytId = 0;
		int layoutResource = 0;
		int inflatedId = 0;
		try {
			frameLytId = idClass.getField("content").getInt(null);
			viewStubId = idClass.getField("action_mode_bar_stub").getInt(null);
			inflatedId = idClass.getField("action_mode_bar").getInt(null);
			layoutResource = layoutClass.getField("action_mode_bar").getInt(
					null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		{
			android.widget.ViewStub viewStub = new android.widget.ViewStub(
					getContext());
			viewStub.setId(viewStubId);
			if (inflatedId != 0)
				viewStub.setInflatedId(inflatedId);
			if (layoutResource != 0)
				viewStub.setLayoutResource(layoutResource);
			lyt.addView(viewStub, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		FrameLayout flyt_content = new FrameLayout(getContext());
		flyt_content.setId(frameLytId);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		// layoutParams.weight = 1;
		flyt_content
				.setForegroundGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP);
		// try {
		// int foreground = attrClass.getField("windowContentOverlay").getInt(
		// null);
		// flyt_content.setForeground(getContext().getResources().getDrawable(
		// foreground));
		// } catch (Throwable e) {
		// e.printStackTrace();
		// }
		lyt.addView(flyt_content, layoutParams);
		return lyt;
	}

	/*
	 * private View createLayout_screen_title(int resource, ViewGroup root) {
	 * LinearLayout lyt = new LinearLayout(getContext());
	 * lyt.setOrientation(LinearLayout.VERTICAL); if
	 * (android.os.Build.VERSION.SDK_INT >= 14) { try {
	 * LinearLayout.class.getMethod("setFitsSystemWindows",
	 * boolean.class).invoke(lyt, true); } catch (Throwable e) { } } int
	 * viewStubId = 0; int textViewId = 0; int frameLytId = 0; int
	 * layoutResource = 0; int inflatedId = 0; try { textViewId =
	 * idClass.getField("title").getInt(null); frameLytId =
	 * idClass.getField("content").getInt(null); viewStubId =
	 * idClass.getField("action_mode_bar_stub").getInt(null); inflatedId =
	 * idClass.getField("action_mode_bar").getInt(null); // layoutResource =
	 * layoutClass.getField("action_mode_bar").getInt( // null); } catch
	 * (Throwable e) { e.printStackTrace(); } { android.widget.ViewStub viewStub
	 * = new android.widget.ViewStub( getContext()); viewStub.setId(viewStubId);
	 * viewStub.setInflatedId(inflatedId); if (layoutResource != 0) {
	 * viewStub.setLayoutResource(layoutResource); } lyt.addView(viewStub, new
	 * LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
	 * LinearLayout.LayoutParams.WRAP_CONTENT)); } { FrameLayout flyt_top = new
	 * FrameLayout(getContext()); int windowTitleSize =
	 * LinearLayout.LayoutParams.WRAP_CONTENT; // try { // windowTitleSize =
	 * attrClass.getField("windowTitleSize").getInt( // null); // } catch
	 * (Throwable e) { // e.printStackTrace(); // } TextView textView = new
	 * TextView(getContext()); textView.setId(textViewId);
	 * textView.setGravity(Gravity.CENTER_VERTICAL); flyt_top.addView(textView,
	 * new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT,
	 * FrameLayout.LayoutParams.MATCH_PARENT)); lyt.addView(flyt_top, new
	 * LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
	 * windowTitleSize)); }
	 * 
	 * FrameLayout flyt_content = new FrameLayout(getContext());
	 * flyt_content.setId(frameLytId); LinearLayout.LayoutParams layoutParams =
	 * new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,
	 * 0); layoutParams.weight = 1; flyt_content
	 * .setForegroundGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP); // try { //
	 * int foreground = attrClass.getField("windowContentOverlay").getInt( //
	 * null); //
	 * flyt_content.setForeground(getContext().getResources().getDrawable( //
	 * foreground)); // } catch (Throwable e) { // e.printStackTrace(); // }
	 * lyt.addView(flyt_content, layoutParams); return lyt; }
	 */

	@Override
	public View inflate(XmlPullParser parser, ViewGroup root) {
		return target.inflate(parser, root);
	}

	@Override
	public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
		return target.inflate(resource, root, attachToRoot);
	}

	@Override
	public View inflate(XmlPullParser parser, ViewGroup root,
			boolean attachToRoot) {
		return target.inflate(parser, root, attachToRoot);
	}

	@Override
	protected View onCreateView(String name, AttributeSet attrs)
			throws ClassNotFoundException {
		try {
			return (View) LayoutInflater.class.getDeclaredMethod(
					"onCreateView", String.class, AttributeSet.class).invoke(
					target, name, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return super.onCreateView(name, attrs);
		}
	}

	protected View onCreateView(View parent, String name, AttributeSet attrs)
			throws ClassNotFoundException {
		try {
			return (View) LayoutInflater.class.getDeclaredMethod(
					"onCreateView", View.class, String.class,
					AttributeSet.class).invoke(target, parent, name, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return super.onCreateView(name, attrs);
		}
	}

}
