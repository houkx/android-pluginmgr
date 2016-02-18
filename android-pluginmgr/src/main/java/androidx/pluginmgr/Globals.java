package androidx.pluginmgr;

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


    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////  破壳系统 常量 //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    public static final String GET_HOST_CONTEXT = "GetHostContext";

    public static final String GET_HOST_RESOURCE = "GetHostRes";

    public static final String GET_HOST_ASSETS = "GetHostAssets";

    public static final String GET_HOST_CLASS_LOADER = "GetHostClassLoader";

    public static final String GET_PLUGIN_PATH = "GetPluginPath";

    public static final String GET_PLUGIN_PKG_NAME = "GetPluginPkgName";

    public static final String GET_PLUGIN_PKG_INFO = "GetPluginPkgInfo";

}
