package org.svnee.easyfile.common.util;

/**
 * 分页工具类
 *
 * @author svnee
 */
public class PageUtil {

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

}
