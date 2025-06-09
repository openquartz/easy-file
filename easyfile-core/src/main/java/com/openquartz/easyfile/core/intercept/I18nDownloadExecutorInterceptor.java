package com.openquartz.easyfile.core.intercept;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.i18n.LocaleContext;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.storage.download.DownloadStorageService;

import java.util.Locale;

/**
 * I18nDownloadExecutorInterceptor
 *
 * @author svnee
 */
public class I18nDownloadExecutorInterceptor implements DownloadExecutorInterceptor {

    private final DownloadStorageService downloadStorageService;

    public I18nDownloadExecutorInterceptor(DownloadStorageService downloadStorageService) {
        this.downloadStorageService = downloadStorageService;
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void beforeExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, Long registerId, InterceptorContext interceptorContext) {

        Locale currentLocale = downloadStorageService.getCurrentLocale(registerId);
        LocaleContext.setCurrentLocale(currentLocale);

    }

    @Override
    public void afterExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, ExportResult result, InterceptorContext interceptorContext) {
        LocaleContext.clear();
    }
}
