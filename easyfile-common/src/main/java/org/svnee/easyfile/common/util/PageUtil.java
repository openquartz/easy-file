package org.svnee.easyfile.common.util;

/**
 * 分页工具类
 *
 * @author svnee
 */
public final class PageUtil {

    private PageUtil() {
    }

    /**
     * 根据总数计算总页数
     *
     * @param totalCount 总数
     * @param pageSize 每页数
     * @return 总页数
     */
    public static int totalPage(int totalCount, int pageSize) {
        if (pageSize == 0) {
            return 0;
        }
        return totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
    }

    /**
     * 计算开始下标
     *
     * @param pageNum 页码
     * @param pageSize 页面条数
     * @return 起始下标
     */
    public static int startIndex(int pageNum, int pageSize) {
        if (pageNum <= 1) {
            return 0;
        }
        return (pageNum - 1) * pageSize;
    }

}
