package com.openquartz.easyfile.storage.remote.impl;

/**
 * 远程服务URL
 *
 * @author svnee
 **/
public final class RemoteUrlConstants {

    private RemoteUrlConstants() {
    }

    public static final String DOWNLOAD_REGISTER_URL = "/easyfile/download/register";

    public static final String TASK_AUTO_REGISTER_URL = "/easyfile/task/autoRegister";

    public static final String DOWNLOAD_CALLBACK_URL = "/easyfile/download/callback";

    public static final String DOWNLOAD_LIMITING_URL = "/easyfile/download/limiting";

    public static final String DOWNLOAD_ENABLE_RUNNING_URL = "/easyfile/download/enableRunning";

    public static final String DOWNLOAD_RECORD_URL = "/easyfile/download/listExport";

    public static final String DOWNLOAD_FILE_URL = "/easyfile/download/file";

    public static final String DOWNLOAD_CANCEL_URL = "/easyfile/download/cancel";

    public static final String LOADING_EXPORT_CACHE_RESULT_URL = "/easyfile/download/loadingCache";

    public static final String GET_REQUEST_INFO_RESULT_URL = "/easyfile/download/getRequestInfo";

    public static final String RESET_EXECUTE_PROCESS_URL = "/easyfile/download/restExecuteProcess";

    public static final String REFRESH_EXECUTE_PROCESS_URL = "/easyfile/download/refreshExecuteProcess";

    public static final String GET_APP_TREE_URL = "/easyfile/download/getAppTree";

    public static final String GET_LOCALE_URL = "/easyfile/download/getLocale";
}
