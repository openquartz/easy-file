package com.openquartz.easyfile.common.request;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 加载导出缓存请求
 *
 * @author svnee
 **/
@Data
public class LoadingExportCacheRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 导出cache-key 表达式
     */
    private List<String> cacheKeyList;

    /**
     * 导出参数Map
     */
    private Map<String, Object> exportParamMap;

    /**
     * 下载操作人
     */
    private String downloadOperateBy;

}
