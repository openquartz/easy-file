package com.openquartz.easyfile.core.executor;

import java.util.List;

/**
 * 导入模板
 * @author svnee
 */
public interface BaseImportExecutor<T> {

    /**
     * 导入数据逻辑
     *
     * @param dataList data list
     */
    void importData(List<T> dataList);

}
