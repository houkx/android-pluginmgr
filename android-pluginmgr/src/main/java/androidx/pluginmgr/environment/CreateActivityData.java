package androidx.pluginmgr.environment;

import java.io.Serializable;

/**
 * 存储一个将要创建的插件Activity的数据
 *
 * @author Lody
 * @version 1.0
 */
public final class CreateActivityData implements Serializable {
    /**
     * 要创建的Activity的类名
     */
    public String activityName;

    /**
     * 插件的ID或包名
     */
    public String pluginPkg;

    public CreateActivityData(String activityName, String pluginPkg) {
        this.activityName = activityName;
        this.pluginPkg = pluginPkg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreateActivityData that = (CreateActivityData) o;

        if (activityName != null ? !activityName.equals(that.activityName) : that.activityName != null)
            return false;
        return !(pluginPkg != null ? !pluginPkg.equals(that.pluginPkg) : that.pluginPkg != null);

    }

    @Override
    public int hashCode() {
        int result = activityName != null ? activityName.hashCode() : 0;
        result = 31 * result + (pluginPkg != null ? pluginPkg.hashCode() : 0);
        return result;
    }
}
