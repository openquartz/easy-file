package org.svnee.easyfile.common.bean;

import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;
import org.svnee.easyfile.common.bean.excel.ExcelBean;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * 当前Sheet的sheetName
 *
 * @author svnee
 **/
public class SheetContext {

    private static final ThreadLocal<String> SHEET_CONTEXT = new NamedThreadLocal<>("SheetContext");

    private SheetContext() {
    }

    public static String currentSheet() throws IllegalStateException {
        String sheet = SHEET_CONTEXT.get();
        if (StringUtils.isBlank(sheet)) {
            return ExcelBean.DEFAULT_SHEET_GROUP;
        }
        return sheet;
    }

    public static String setSheet(@Nullable String sheet) {
        String old = SHEET_CONTEXT.get();
        if (sheet != null) {
            SHEET_CONTEXT.set(sheet);
        } else {
            SHEET_CONTEXT.remove();
        }
        return old;
    }

}
