package com.openquartz.easyfile.core.executor.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.NamedThreadLocal;

/**
 * @author svnee
 */
public class FileExportTriggerContext {

    private static final ThreadLocal<Map<String, Object>> currentFileExtend = new NamedThreadLocal<>(
        "Current File export");

    private static final String ASYNC_TRIGGER_FLAG = "ASYNC_TRIGGER";

    private FileExportTriggerContext() {
    }

    public static void setAsyncTriggerFlagIfAbsent(boolean flag) {
        if (currentFileExtend.get() != null) {
            currentFileExtend.get().putIfAbsent(ASYNC_TRIGGER_FLAG, flag);
        } else {
            Map<String, Object> extMap = new HashMap<>();
            extMap.putIfAbsent(ASYNC_TRIGGER_FLAG, flag);
            currentFileExtend.set(extMap);
        }
    }

    public static boolean isAsyncTrigger(){
        return (boolean) Optional.ofNullable(currentFileExtend.get())
            .map(e->e.get(ASYNC_TRIGGER_FLAG))
            .orElse(false);

    }

    public static void clear() {
        currentFileExtend.remove();
    }
}
