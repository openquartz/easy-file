package com.openquartz.easyfile.core.property;

import com.openquartz.easyfile.common.property.IEasyFileCommonProperties;

/**
 * DownloadProperty
 *
 * @author svnee
 * @since 1.2.2
 */
public interface IEasyFileDownloadProperty extends IEasyFileCommonProperties {

    /**
     * clean file after upload completed
     *
     * @return clean flag
     */
    boolean isCleanFileAfterUpload();

    /**
     * local file temp path default /temp
     *
     * @return local path
     */
    String getLocalFileTempPath();

    /**
     * enable compress file before upload
     *
     * @return compress flag
     */
    boolean isEnableCompressFile();

    /**
     * min enable compress file condition(file size)
     *
     * @return file size mb
     */
    int getMinEnableCompressMbSize();
}
