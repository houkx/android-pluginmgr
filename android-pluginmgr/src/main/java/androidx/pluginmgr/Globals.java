package androidx.pluginmgr;

import android.content.pm.ActivityInfo;

/**
 * 全局声明类
 *
 * @author Lody
 * @version 1.0
 */
public class Globals {

    /**
     * 私有目录中保存插件文件的文件夹名
     */
    public static final String PRIVATE_PLUGIN_OUTPUT_DIR_NAME = "plugins-file";

    /**
     * 私有目录中保存插件odex的文件夹名
     */
    public static final String PRIVATE_PLUGIN_ODEX_OUTPUT_DIR_NAME = "plugins-opt";

    /**
     * Activity来自插件的标志
     */
    public static final String FLAG_ACTIVITY_FROM_PLUGIN = "flag_act_fp";


    /**
     * TODO:根据插件特征选择Activity
     *
     * @param activityInfo 插件ActivityInfo信息
     * @return 选择的Activity
     */
    public static Class<?> selectDynamicActivity(ActivityInfo activityInfo) {
        return DynamicActivity.class;
    }

}
