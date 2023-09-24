package com.openquartz.easyfile.starter.processor;

import com.openquartz.easyfile.core.intercept.ExportExecutorInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import com.openquartz.easyfile.core.intercept.ExecutorInterceptorSupport;

/**
 * 下载执行器后置处理Bean
 *
 * @author svnee
 */
@Slf4j
public class DownloadInterceptorPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof ExportExecutorInterceptor) {
            ExecutorInterceptorSupport.putIfAbsent((ExportExecutorInterceptor) bean);
        }
        return bean;
    }
}