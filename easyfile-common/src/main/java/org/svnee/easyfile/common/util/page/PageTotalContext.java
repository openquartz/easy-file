package org.svnee.easyfile.common.util.page;

import java.util.Map;
import java.util.Objects;
import org.svnee.easyfile.common.bean.excel.ExcelBean;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.page.PageTotal;

/**
 * PageTotalContext
 *
 * @author svnee
 */
public final class PageTotalContext {

    private static final ThreadLocal<Map<Object, PageTotal>> CURRENT_PAGE_TOTAL =
        ThreadLocal.withInitial(() -> MapUtils.newHashMapWithExpectedSize(10));

    private PageTotalContext() {
    }

    public static PageTotal currentPageToTal(Object sheetGroup) {
        Map<Object, PageTotal> totalMap = currentAllPageTotal();
        return totalMap.get(sheetGroup);
    }

    public static PageTotal currentPageToTal() {
        return currentPageToTal(ExcelBean.DEFAULT_SHEET_GROUP);
    }

    public static Map<Object, PageTotal> currentAllPageTotal() {
        Map<Object, PageTotal> pageTotalMap = CURRENT_PAGE_TOTAL.get();
        if (Objects.isNull(pageTotalMap)) {
            CURRENT_PAGE_TOTAL.set(MapUtils.newHashMapWithExpectedSize(10));
        }
        return CURRENT_PAGE_TOTAL.get();
    }

    /**
     * 缓存PageTotal
     *
     * @param pageTotal pageTotal
     */
    public static void cache(PageTotal pageTotal) {
        if (pageTotal != null) {
            currentAllPageTotal().put(ExcelBean.DEFAULT_SHEET_GROUP, pageTotal);
        }
    }

    /**
     * 缓存PageTotal
     *
     * @param pageTotal pageTotal
     */
    public static void cache(Object sheetGroup, PageTotal pageTotal) {
        if (pageTotal != null) {
            Map<Object, PageTotal> allPageTotal = currentAllPageTotal();
            allPageTotal.put(sheetGroup, pageTotal);
        }
    }

    /**
     * 清除
     */
    public static void clear() {
        CURRENT_PAGE_TOTAL.remove();
    }

}
