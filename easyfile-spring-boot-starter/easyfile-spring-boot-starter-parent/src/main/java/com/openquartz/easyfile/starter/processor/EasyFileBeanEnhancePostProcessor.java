package com.openquartz.easyfile.starter.processor;

import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.file.FileUrlTransformer;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.core.exception.ExportErrorCode;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.storage.expand.FileUrlTransformerSupport;

/**
 * 异步文件执行
 *
 * @author svnee
 */
public class EasyFileBeanEnhancePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

        Class<?> clazz = SpringContextUtil.getRealClass(bean);

        if (clazz.isAnnotationPresent(FileExportExecutor.class)) {
            if (!BaseExportExecutor.class.isAssignableFrom(clazz)) {
                throw EasyFileException.replacePlaceHold(ExportErrorCode.BASE_DOWNLOAD_EXECUTOR_IMPL_ILL_ERROR,
                        BaseExportExecutor.class);
            }
            FileExportExecutor exportExecutor = clazz.getAnnotation(FileExportExecutor.class);
            if (FileExportExecutorSupport.contains(exportExecutor.value())
                    && !clazz.equals(FileExportExecutorSupport.get(exportExecutor.value()).getClass())) {
                throw EasyFileException
                        .replacePlaceHold(ExportErrorCode.DOWNLOAD_CODE_NOT_UNIQ_ERROR, exportExecutor.value());
            }
            FileExportExecutorSupport.register(exportExecutor.value(), (BaseExportExecutor) bean);
        }

        // 注册file-url-transformer
        if (FileUrlTransformer.class.isAssignableFrom(clazz)) {
            FileUrlTransformerSupport.register((FileUrlTransformer) bean);
        }
        return bean;
    }
}
