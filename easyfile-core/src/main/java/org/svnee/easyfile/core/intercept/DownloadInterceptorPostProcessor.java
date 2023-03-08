package org.svnee.easyfile.core.intercept;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 下载执行器后置处理Bean
 *
 * @author svnee
 */
@Component
public class DownloadInterceptorPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DownloadExecutorInterceptor) {
            ExecutorInterceptorSupport.putIfAbsent((DownloadExecutorInterceptor) bean);
        }
        return bean;
    }
}