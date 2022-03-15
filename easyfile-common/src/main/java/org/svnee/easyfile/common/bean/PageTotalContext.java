package org.svnee.easyfile.common.bean;

import org.springframework.core.NamedThreadLocal;

/**
 * PageTotalContext
 *
 * @author svnee
 */
public final class PageTotalContext {

    private static final ThreadLocal<PageTotal> currentPageToTal = new NamedThreadLocal<>("Current Page Total");

    private PageTotalContext() {
    }

    public static PageTotal currentPageToTal() {
        return currentPageToTal.get();
    }

    /**
     * 缓存PageTotal
     *
     * @param pageTotal pageTotal
     */
    public static void cache(PageTotal pageTotal) {
        if (pageTotal != null) {
            currentPageToTal.set(pageTotal);
        }
    }

    /**
     * 清除
     */
    public static void clear() {
        currentPageToTal.remove();
    }

}
