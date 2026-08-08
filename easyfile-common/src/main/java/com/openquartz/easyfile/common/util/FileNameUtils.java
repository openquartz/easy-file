package com.openquartz.easyfile.common.util;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.FileSuffixEnum;

/**
 * File name helpers.
 *
 * @author svnee
 */
public final class FileNameUtils {

    private FileNameUtils() {
    }

    /**
     * Whether the file name already indicates a zip archive.
     *
     * @param fileName file name
     * @return true if extension is zip
     */
    public static boolean isZipCompress(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(Constants.FILE_SUFFIX_SEPARATOR)) {
            return false;
        }
        String[] fileNameSplit = fileName.split("\\.");
        if (fileNameSplit.length <= 1) {
            return false;
        }
        String suffix = fileNameSplit[fileNameSplit.length - 1];
        return FileSuffixEnum.ZIP.getCode().equalsIgnoreCase(suffix);
    }
}
