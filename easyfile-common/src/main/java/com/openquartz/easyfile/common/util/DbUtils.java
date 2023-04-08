package com.openquartz.easyfile.common.util;

import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.DataExecuteErrorCode;

/**
 * 数据校验工具类
 *
 * @author svnee
 **/
public final class DbUtils {

    private DbUtils() {
    }

    public static void checkInsertedOneRow(int affect) {
        checkInsertedRows(1, affect);
    }

    public static void checkInsertedRows(int expect, int affect) {
        Asserts.isTrue(affect == expect, DataExecuteErrorCode.INSERT_ERROR);
    }

    public static void checkUpdatedRows(int expect, int affect) {
        Asserts.isTrue(affect == expect, DataExecuteErrorCode.UPDATE_ERROR);
    }

    public static void checkUpdatedOneRow(int affect) {
        checkUpdatedRows(1, affect);
    }

    public static void checkDeletedOneRow(int affect) {
        Asserts.isTrue(affect == 1, DataExecuteErrorCode.DELETE_ERROR);
    }

}
