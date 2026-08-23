package com.openquartz.easyfile.server.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.BaseExecuteParam;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.server.config.BizConfig;
import com.openquartz.easyfile.server.entity.AsyncDownloadRecord;
import com.openquartz.easyfile.server.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.server.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.server.service.NotifyService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDownloadServiceImplGetRequestInfoTest {

    @Mock
    private AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    @Mock
    private AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    @Mock
    private NotifyService notifyService;
    @Mock
    private BizConfig bizConfig;
    @InjectMocks
    private AsyncDownloadServiceImpl asyncDownloadService;

    @Test
    public void getRequestInfoByRegisterId_legacyBaseExecuteParam_buildsFromRecordColumns() {
        BaseExecuteParam param = new BaseExecuteParam();
        param.setDownloadCode("code-legacy");
        param.setFileSuffix("xlsx");
        Map<String, Object> otherMap = new HashMap<String, Object>();
        otherMap.put("k1", "v1");
        param.setOtherMap(otherMap);

        AsyncDownloadRecord record = new AsyncDownloadRecord();
        record.setId(11L);
        record.setDownloadCode("code-from-record");
        record.setRemark("export-remark");
        record.setDownloadOperateBy("user-by");
        record.setDownloadOperateName("User Name");
        record.setNotifyEmail("u@example.com");
        record.setExecuteParam(JSONUtil.toClassJson(param));

        when(asyncDownloadRecordMapper.findById(11L)).thenReturn(record);

        DownloadRequestInfo info = asyncDownloadService.getRequestInfoByRegisterId(11L);

        assertNotNull(info);
        assertEquals("code-from-record", info.getDownloadCode());
        assertNotNull(info.getRequestContext());
        assertEquals("xlsx", info.getRequestContext().getFileSuffix());
        assertEquals(Collections.singletonMap("k1", "v1"), info.getRequestContext().getOtherMap());
        assertEquals("export-remark", info.getRequestContext().getExportRemark());
        assertNotNull(info.getRequestContext().getNotifier());
        assertEquals("user-by", info.getRequestContext().getNotifier().getUserBy());
        assertEquals("User Name", info.getRequestContext().getNotifier().getUserName());
        assertEquals("u@example.com", info.getRequestContext().getNotifier().getEmail());
    }

    @Test
    public void getRequestInfoByRegisterId_registerDownloadRequest_preservesNotifierAndRemark() {
        RegisterDownloadRequest request = new RegisterDownloadRequest();
        request.setDownloadCode("code-new");
        request.setFileSuffix("csv");
        Map<String, Object> otherMap = new HashMap<String, Object>();
        otherMap.put("page", 1);
        request.setOtherMap(otherMap);
        request.setExportRemark("full-request-remark");
        Notifier notifier = new Notifier();
        notifier.setUserBy("op");
        notifier.setUserName("Operator");
        notifier.setEmail("op@example.com");
        request.setNotifier(notifier);
        request.setAppId("app-1");
        request.setEnableNotify(Boolean.TRUE);

        AsyncDownloadRecord record = new AsyncDownloadRecord();
        record.setId(22L);
        record.setDownloadCode("code-from-record");
        record.setRemark("should-not-override");
        record.setDownloadOperateBy("ignored");
        record.setExecuteParam(JSONUtil.toClassJson(request));

        when(asyncDownloadRecordMapper.findById(22L)).thenReturn(record);

        DownloadRequestInfo info = asyncDownloadService.getRequestInfoByRegisterId(22L);

        assertNotNull(info);
        assertEquals("code-from-record", info.getDownloadCode());
        assertNotNull(info.getRequestContext());
        assertEquals("csv", info.getRequestContext().getFileSuffix());
        assertEquals(Collections.singletonMap("page", 1), info.getRequestContext().getOtherMap());
        assertEquals("full-request-remark", info.getRequestContext().getExportRemark());
        assertNotNull(info.getRequestContext().getNotifier());
        assertEquals("op", info.getRequestContext().getNotifier().getUserBy());
        assertEquals("Operator", info.getRequestContext().getNotifier().getUserName());
        assertEquals("op@example.com", info.getRequestContext().getNotifier().getEmail());
    }
}
