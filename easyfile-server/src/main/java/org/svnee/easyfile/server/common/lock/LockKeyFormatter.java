package org.svnee.easyfile.server.common.lock;

import java.text.MessageFormat;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.svnee.easyfile.common.constants.Constants;

/**
 * 分布式锁Formatter
 *
 * @author svnee
 */
@Getter
@AllArgsConstructor
public enum LockKeyFormatter {

    /**
     * 下载运行锁
     * 0: 下载记录注册ID
     */
    DOWNLOAD_RUNNING_LOCK("download_running", "{0}", "string", "下载运行锁"),

    /**
     * 下载任务自动注册锁
     * 0：服务appId
     */
    TASK_AUTO_REGISTER_LOCK("task_auto_register", "{0}", "string", "下载任务自动注册锁"),

    ;

    /**
     * 前缀
     */
    private final String keyPrefix;

    /**
     * 模式
     */
    private final String pattern;

    /**
     * 存储类型
     */
    private final String storageType;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 生成lockKey
     *
     * @param applicationName appId
     * @param placeHolder placeHolder
     * @return lockKey
     */
    public String generateLockKey(String applicationName, Object... placeHolder) {
        StringJoiner joiner = new StringJoiner(Constants.COLON_SPLITTER);
        joiner.add(applicationName);
        joiner.add(this.getKeyPrefix());
        joiner.add(MessageFormat.format(this.getPattern(), placeHolder));
        joiner.add(this.getStorageType());
        return joiner.toString();
    }


}
