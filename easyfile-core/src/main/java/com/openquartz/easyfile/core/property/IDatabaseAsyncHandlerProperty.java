package com.openquartz.easyfile.core.property;

/**
 * IDatabaseAsyncHandlerProperty
 *
 * @author svnee
 * @since 1.2.2
 */
public interface IDatabaseAsyncHandlerProperty {

    /**
     * 回溯时间
     * 单位：小时
     *
     * @return 回溯时间
     */
    default Integer getLookBackHours() {
        return 2;
    }

    /**
     * 最大重试次数
     *
     * @return 最大重试次数
     */
    default Integer getMaxTriggerCount() {
        return 5;
    }

    /**
     * 最大归档小时
     *
     * @return 超时归档时间
     */
    default Integer getMaxArchiveHours() {
        return 24;
    }

    /**
     * 最大超时时间
     *
     * @return 执行超时时间
     */
    default Integer getMaxExecuteTimeout() {
        return 1600;
    }

    /**
     * 调度周期
     * 单位：秒
     *
     * @return 调度周期
     */
    default Integer getSchedulePeriod() {
        return 10;
    }

}
