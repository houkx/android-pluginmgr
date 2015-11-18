package androidx.pluginmgr.environment;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Window;

import java.lang.reflect.Field;

import androidx.pluginmgr.Globals;
import androidx.pluginmgr.PluginManager;
import androidx.pluginmgr.delegate.DelegateInstrumentation;
import androidx.pluginmgr.reflect.Reflect;
import androidx.pluginmgr.verify.PluginNotFoundException;
import androidx.pluginmgr.widget.LayoutInflaterWrapper;

/**
 * @author Lody
 * @version 1.0
 */
public class PluginInstrumentation extends DelegateInstrumentation
{

    /**
     * 当前正在运行的插件
     */
    private PlugInfo currentPlugin;

    /**
     * @param mBase 真正的Instrumentation
     */
    public PluginInstrumentation(Instrumentation mBase)
	{
        super(mBase);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
        CreateActivityData activityData = (CreateActivityData) intent.getSerializableExtra(Globals.FLAG_ACTIVITY_FROM_PLUGIN);
        //如果activityData存在,那么说明将要创建的是插件Activity
        if (activityData != null)
		{
            //这里找不到插件信息就会抛异常的,不用担心空指针
            PlugInfo plugInfo;
            try
			{
                plugInfo = PluginManager.getSingleton().tryGetPluginInfo(activityData.pluginPkg);
            }
			catch (PluginNotFoundException e)
			{
                PluginManager.getSingleton().dump();
                throw new IllegalAccessException("Cannot get plugin Info : " + activityData.pluginPkg);
            }
            if (activityData.activityName != null)
			{
                className = activityData.activityName;
                cl = plugInfo.getClassLoader();
            }
        }
        return super.newActivity(cl, className, intent);
    }


    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle)
	{
        lookupActivityInPlugin(activity);
        if (currentPlugin != null)
		{
            //初始化插件Activity
            Context baseContext = activity.getBaseContext();
            PluginContext pluginContext = new PluginContext(baseContext, currentPlugin);
            try
			{
				try
				{
					//在许多设备上，Activity自身hold资源
					Reflect.on(activity).set("mResources", pluginContext.getResources());
				}
				catch (Throwable ignored)
				{}

                Field field = ContextWrapper.class.getDeclaredField("mBase");
                field.setAccessible(true);
                field.set(activity, pluginContext);

				Reflect.on(activity).set("mApplication", currentPlugin.getApplication());

            }
			catch (Throwable e)
			{
                e.printStackTrace();
            }

            ActivityInfo activityInfo = currentPlugin.findActivityByClassName(activity.getClass().getName());
            if (activityInfo != null)
				{
                    //根据AndroidManifest.xml中的参数设置Theme
                    int resTheme = activityInfo.getThemeResource();
                    if (resTheme != 0) {
                        boolean hasNotSetTheme = true;
                        try {
                            Field mTheme = ContextThemeWrapper.class
                                    .getDeclaredField("mTheme");
                            mTheme.setAccessible(true);
                            hasNotSetTheme = mTheme.get(activity) == null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (hasNotSetTheme) {
                            changeActivityInfo(activityInfo, activity);
                            activity.setTheme(resTheme);
                        }
                    }

                }

            // 如果是三星手机，则使用包装的LayoutInflater替换原LayoutInflater
            // 这款手机在解析内置的布局文件时有各种错误
            if (android.os.Build.MODEL.startsWith("GT")) {
                Window window = activity.getWindow();
                Reflect windowRef = Reflect.on(window);
                try {
                    LayoutInflater originInflater = window.getLayoutInflater();
                    if (!(originInflater instanceof LayoutInflaterWrapper)) {
                        windowRef.set("mLayoutInflater", new LayoutInflaterWrapper(originInflater));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }


        super.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callActivityOnResume(Activity activity)
	{
        lookupActivityInPlugin(activity);
        super.callActivityOnResume(activity);
    }

    private static void changeActivityInfo(ActivityInfo activityInfo, Activity activity) {
        Field field_mActivityInfo;
        try {
            field_mActivityInfo = Activity.class.getDeclaredField("mActivityInfo");
            field_mActivityInfo.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            field_mActivityInfo.set(activity, activityInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void callActivityOnDestroy(Activity activity)
	{
        super.callActivityOnDestroy(activity);
    }

    /**
     * 检查跳转目标是不是来自插件
     *
     * @param activity Activity
     */
    private void lookupActivityInPlugin(Activity activity)
	{
        ClassLoader classLoader = activity.getClass().getClassLoader();
        if (classLoader instanceof PluginClassLoader)
		{
            currentPlugin = ((PluginClassLoader) classLoader).getPlugInfo();
        }
		else
		{
            currentPlugin = null;
        }
    }

    private void replaceIntentTargetIfNeed(Context from, Intent intent)
	{
        if (!intent.hasExtra(Globals.FLAG_ACTIVITY_FROM_PLUGIN) && currentPlugin != null)
		{
            ComponentName componentName = intent.getComponent();
            if (componentName != null)
			{
                String pkgName = componentName.getPackageName();
                String activityName = componentName.getClassName();
                if (pkgName != null)
				{
                    CreateActivityData createActivityData = new CreateActivityData(activityName, currentPlugin.getPackageName());
                    intent.setClass(from, Globals.selectDynamicActivity(currentPlugin.findActivityByClassName(activityName)));
                    intent.putExtra(Globals.FLAG_ACTIVITY_FROM_PLUGIN, createActivityData);
                }
            }
        }
    }


    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment fragment, Intent intent, int requestCode)
	{
        replaceIntentTargetIfNeed(who, intent);
        return super.execStartActivity(who, contextThread, token, fragment, intent, requestCode);
    }


    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment fragment, Intent intent, int requestCode, Bundle options)
	{
        replaceIntentTargetIfNeed(who, intent);
        return super.execStartActivity(who, contextThread, token, fragment, intent, requestCode, options);
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode)
	{
        replaceIntentTargetIfNeed(who, intent);
        return super.execStartActivity(who, contextThread, token, target, intent, requestCode);
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options)
	{
        replaceIntentTargetIfNeed(who, intent);
        return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    }
}
