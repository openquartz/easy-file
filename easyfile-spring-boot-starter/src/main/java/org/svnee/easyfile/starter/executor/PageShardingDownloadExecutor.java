package org.svnee.easyfile.starter.executor;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;

/**
 * 按页分片下载执行器
 * <p>
 *
 * @author svnee
 */
public interface PageShardingDownloadExecutor<T> extends BaseDownloadExecutor {

    /**
     * 用于传递参数
     *
     * @param othersMap 参数map
     * @return 计算分页信息
     */
    PageTotal count(Map<String, Object> othersMap);

    /**
     * 分页导出
     *
     * @param context 上下文请求
     * @param page 页
     * @param maxId 上一页的最大的ID
     * @return 当前最大ID maxId --> resultList
     */
    Pair<Long, List<T>> shardingData(DownloaderRequestContext context, Page page, Long maxId);

}
