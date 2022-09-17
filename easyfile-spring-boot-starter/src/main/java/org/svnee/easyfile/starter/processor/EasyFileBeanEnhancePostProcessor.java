package org.svnee.easyfile.starter.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.svnee.easyfile.common.annotations.FileExportExecutor;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.file.FileUrlTransformer;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.starter.exception.DownloadErrorCode;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.storage.expand.FileUrlTransformerSupport;

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
