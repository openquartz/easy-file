package org.svnee.easyfile.server.utils;

import org.svnee.easyfile.common.exception.AssertUtil;
import org.svnee.easyfile.common.exception.DataExecuteErrorCode;

/**
 * 数据校验工具类
 *
 * @author svnee
 **/
public final class DbUtils {

    private DbUtils() {
    }

    public static void checkInsertedOneRows(int affect) {
        AssertUtil.isTrue(affect == 1, DataExecuteErrorCode.INSERT_ERROR);
    }

    public static void checkUpdatedOneRows(int affect) {
        AssertUtil.isTrue(affect == 1, DataExecuteErrorCode.UPDATE_ERROR);
    }


}
