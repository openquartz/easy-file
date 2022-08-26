package org.svnee.easyfile.starter.executor;

import java.util.List;
import java.util.Map;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.Page;
import org.svnee.easyfile.common.bean.PageTotal;
import org.svnee.easyfile.common.bean.Pair;

/**
 * 按页分片下载执行器
 * <T> : 下载模版类
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
     * @param cursorId 当前滚动的分页的游标ID,可以是使用ID 做最大最小值传递。主要是用于优化传递分页查询速度
     * @return 当前最大ID cursorId --> resultList
     */
    Pair<Long, List<T>> shardingData(BaseDownloaderRequestContext context, Page page, Long cursorId);

}
