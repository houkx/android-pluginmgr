package androidx.pluginmgr.verify;

import java.io.File;

/**
 * 插件需要拷贝到私有目录下,
 * 如果私有目录下,已经有一个文件存在,那么就需要是否还有必要覆盖原文件.
 *
 * @author Lody
 * @version 1.0
 */
public interface PluginOverdueVerifier {

    /**
     * 检查已存在的目标插件是否已经过期
     *
     * @param originPluginFile 原插件文件
     * @param targetExistFile  已存在的目标插件文件
     * @return 私有目录下已存在的目标插件是否已经过期
     */
    boolean isOverdue(File originPluginFile, File targetExistFile);
}
