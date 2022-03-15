package org.svnee.easyfile.core.intercept;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 执行器拦截器
 *
 * @author xuzhao
 */
public class ExecutorInterceptorSupport {

    private ExecutorInterceptorSupport() {
    }

    private static final Set<DownloadExecutorInterceptor> INTERCEPTOR_LIST = new LinkedHashSet<>();

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public static synchronized void putIfAbsent(DownloadExecutorInterceptor interceptor) {
        INTERCEPTOR_LIST.add(interceptor);
    }

    /**
     * 获取拦截器
     *
     * @return 拦截器
     */
    public static Set<DownloadExecutorInterceptor> getInterceptors() {
        return INTERCEPTOR_LIST;
    }
}
