package androidx.pluginmgr.utils;

import android.util.Log;

/**
 * @author Lody
 * @version 1.0
 */
public class Trace {

    /**
     * 是否要输出到LogCat
     */
    public static boolean LOG_OUTPUT = true;


    private static final StringBuilder _TRACE_BUILDER = new StringBuilder(100);

    public static void store(Object msg) {

        String msgStr = object2Msg(msg);

        _TRACE_BUILDER.append(msgStr).append("\n");

        if (LOG_OUTPUT) {
            Log.d("PluginMgr-Trace", msgStr);
        }
    }


    public static String getTrace() {
        return _TRACE_BUILDER.toString();
    }

    public static void clearTrace() {
        _TRACE_BUILDER.delete(0, _TRACE_BUILDER.length() - 1);
    }

    private static String object2Msg(Object object) {
        return object != null ? object.toString() : " ";
    }
}
