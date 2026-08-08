package com.openquartz.easyfile.common.util;

import java.io.File;
import java.io.IOException;

/**
 * Resolve import temp file paths safely (prevent path traversal).
 *
 * @author svnee
 */
public final class ImportTempFiles {

    private ImportTempFiles() {
    }

    /**
     * Resolve a temp file under {@code tempDir}. Rejects names containing path separators or escaping the root.
     *
     * @param tempDir temp directory
     * @param fileName optional client file name; if blank, generated from registerId + fileType
     * @param registerId register id
     * @param fileType file type extension
     * @return file under tempDir
     */
    public static File resolveTempFile(String tempDir, String fileName, Long registerId, String fileType) {
        String safeName;
        if (StringUtils.isBlank(fileName)) {
            safeName = "import_" + registerId + "." + (fileType == null ? "dat" : fileType);
        } else {
            safeName = fileName.trim();
            if (safeName.contains("..") || safeName.contains("/") || safeName.contains("\\")
                || safeName.contains(File.separator)) {
                throw new IllegalArgumentException("非法文件名，存在路径穿越风险: " + fileName);
            }
        }

        File root = new File(tempDir);
        File target = new File(root, safeName);
        try {
            String rootPath = root.getCanonicalPath();
            String targetPath = target.getCanonicalPath();
            if (!targetPath.startsWith(rootPath + File.separator) && !targetPath.equals(rootPath)) {
                throw new IllegalArgumentException("非法文件路径，存在路径穿越风险: " + fileName);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("非法文件路径: " + fileName, e);
        }
        return target;
    }
}
