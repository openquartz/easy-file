package com.openquartz.easyfile.starter.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.file.FileUrlTransformer;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.core.exception.DownloadErrorCode;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
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
            if (!BaseDownloadExecutor.class.isAssignableFrom(clazz)) {
                throw EasyFileException.replacePlaceHold(DownloadErrorCode.BASE_DOWNLOAD_EXECUTOR_IMPL_ILL_ERROR,
                    BaseDownloadExecutor.class);
            }
            FileExportExecutor exportExecutor = clazz.getAnnotation(FileExportExecutor.class);
            if (FileExportExecutorSupport.contains(exportExecutor.value())
                && !clazz.equals(FileExportExecutorSupport.get(exportExecutor.value()).getClass())) {
                throw EasyFileException
                    .replacePlaceHold(DownloadErrorCode.DOWNLOAD_CODE_NOT_UNIQ_ERROR, exportExecutor.value());
            }
            FileExportExecutorSupport.register(exportExecutor.value(), exportExecutor, (BaseDownloadExecutor) bean);
        }

        // 注册file-url-transformer
        if (FileUrlTransformer.class.isAssignableFrom(clazz)) {
            FileUrlTransformerSupport.register((FileUrlTransformer) bean);
        }
        return bean;
    }
}
