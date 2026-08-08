package com.openquartz.easyfile.core.executor.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.NamedThreadLocal;

/**
 * 导入异步触发上下文标记
 *
 * @author svnee
 */
public final class FileImportTriggerContext {

    private static final ThreadLocal<Map<String, Object>> CURRENT_IMPORT_EXTEND = new NamedThreadLocal<>(
        "Current File import");

    private static final String ASYNC_TRIGGER_FLAG = "ASYNC_TRIGGER";

    private FileImportTriggerContext() {
    }

    public static void setAsyncTriggerFlagIfAbsent(boolean flag) {
        if (CURRENT_IMPORT_EXTEND.get() != null) {
            CURRENT_IMPORT_EXTEND.get().putIfAbsent(ASYNC_TRIGGER_FLAG, flag);
        } else {
            Map<String, Object> extMap = new HashMap<>();
            extMap.putIfAbsent(ASYNC_TRIGGER_FLAG, flag);
            CURRENT_IMPORT_EXTEND.set(extMap);
        }
    }

    public static boolean isAsyncTrigger() {
        return (boolean) Optional.ofNullable(CURRENT_IMPORT_EXTEND.get())
            .map(e -> e.get(ASYNC_TRIGGER_FLAG))
            .orElse(false);
    }

    public static void clear() {
        CURRENT_IMPORT_EXTEND.remove();
    }
}
