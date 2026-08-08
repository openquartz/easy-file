package com.openquartz.easyfile.storage.remote.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.storage.remote.common.HttpAgent;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/**
 * Import auto-register must POST /easyfile/import/autoRegister (not download task path).
 */
public class HttpEasyFileClientImportAutoRegisterTest {

    private HttpAgent httpAgent;
    private HttpEasyFileClientImpl client;

    @Before
    public void setUp() {
        httpAgent = mock(HttpAgent.class);
        client = new HttpEasyFileClientImpl(httpAgent);
    }

    @Test
    public void autoRegisterImportTask_postsImportAutoRegisterUrl() {
        AutoTaskRegisterRequest request = new AutoTaskRegisterRequest()
            .setAppId("app")
            .setImportCodeMap(Collections.singletonMap("code", "desc"));
        when(httpAgent.httpPost(RemoteUrlConstants.IMPORT_AUTO_REGISTER_URL, request))
            .thenReturn(ResponseResult.ok());

        client.autoRegisterImportTask(request);

        verify(httpAgent).httpPost(eq(RemoteUrlConstants.IMPORT_AUTO_REGISTER_URL), eq(request));
    }
}
