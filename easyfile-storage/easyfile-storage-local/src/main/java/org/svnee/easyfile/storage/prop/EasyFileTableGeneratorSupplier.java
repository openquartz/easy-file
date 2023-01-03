package org.svnee.easyfile.storage.prop;


import java.text.MessageFormat;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * table formatter supplier
 *
 * @author svnee
 **/
public final class EasyFileTableGeneratorSupplier {

    private EasyFileTableGeneratorSupplier() {
    }

    /**
     * 表名前缀
     */
    private static String prefix = "ef";

    private static final String TABLE_ASYNC_DOWNLOAD_RECORD_FORMAT_PATTERN = "{0}_async_download_record";
    private static final String TABLE_ASYNC_DOWNLOAD_TASK_FORMAT_PATTERN = "{0}_async_download_task";
    private static final String TABLE_ASYNC_DOWNLOAD_TRIGGER_FORMAT_PATTERN = "{0}_async_download_trigger";

    public static void setPrefix(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            EasyFileTableGeneratorSupplier.prefix = prefix;
        }
    }

    public static String genAsyncDownloadRecordTable() {
        return MessageFormat.format(TABLE_ASYNC_DOWNLOAD_RECORD_FORMAT_PATTERN, prefix);
    }

    public static String genAsyncDownloadTaskTable() {
        return MessageFormat.format(TABLE_ASYNC_DOWNLOAD_TASK_FORMAT_PATTERN, prefix);
    }

    public static String genAsyncDownloadTriggerTable() {
        return MessageFormat.format(TABLE_ASYNC_DOWNLOAD_TRIGGER_FORMAT_PATTERN, prefix);
    }
}
