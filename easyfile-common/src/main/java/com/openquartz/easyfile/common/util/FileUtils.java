package com.openquartz.easyfile.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.CommonErrorCode;

/**
 * @author svnee
 **/
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * sizeOfByte
     *
     * @param file file
     * @return byte
     */
    public static long sizeOf(File file) {
        Asserts.notNull(file, CommonErrorCode.FILE_NOT_EXIST_ERROR);
        Asserts.isTrue(file.exists(), CommonErrorCode.FILE_NOT_EXIST_ERROR);
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }

    /**
     * sizeOfMB
     *
     * @param file file
     * @return MB byte
     */
    public static int sizeOfMb(File file) {
        long byteSize = sizeOf(file);
        return BigInteger.valueOf(byteSize).divide(Constants.ONE_MB).intValue();
    }

    /**
     * sizeOfMB
     *
     * @param file file
     * @return MB byte
     */
    public static int sizeOfKb(File file) {
        long byteSize = sizeOf(file);
        return BigInteger.valueOf(byteSize).divide(Constants.ONE_KB).intValue();
    }

    /**
     * sizeOfDirectory0
     *
     * @param directory dictionary
     * @return size
     */
    private static long sizeOfDirectory0(final File directory) {
        Asserts.notNull(directory, CommonErrorCode.FILE_NOT_EXIST_ERROR);
        final File[] files = directory.listFiles();
        // null if security restricted
        if (files == null) {
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size += sizeOf0(file);
                if (size < 0) {
                    break;
                }
            }
        }

        return size;
    }

    public static boolean isSymlink(final File file) {
        return file != null && Files.isSymbolicLink(file.toPath());
    }

    private static long sizeOf0(final File file) {
        Asserts.notNull(file, CommonErrorCode.FILE_NOT_EXIST_ERROR);
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        // will be 0 if file does not exist
        return file.length();
    }

    public static String md5(File file) {
        Asserts.notNull(file, CommonErrorCode.FILE_NOT_EXIST_ERROR);
        Asserts.isTrue(file.exists(), CommonErrorCode.FILE_NOT_EXIST_ERROR);
        Asserts.isTrue(file.isFile(), CommonErrorCode.FILE_NOT_EXIST_ERROR);

        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("计算文件MD5失败", e);
        }
    }

}
