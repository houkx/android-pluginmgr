package android.app;

import android.content.pm.ApplicationInfo;

/**
 * NOTICE:不要混淆本类
 *
 * @author Lody
 * @version 1.0
 */
public final class ActivityThread {

    /**
     * NOTICE: 必须在UI线程调用本方法,否则返回NULL
     *
     * @return
     */
    public static ActivityThread currentActivityThread() {
        return null;
    }

    public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai) {
        return null;
    }
}
