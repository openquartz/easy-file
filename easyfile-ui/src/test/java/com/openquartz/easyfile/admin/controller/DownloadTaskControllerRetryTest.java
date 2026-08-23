package com.openquartz.easyfile.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.admin.model.request.RetryDownloadTaskRequest;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.service.ServerAppIdProvider;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadTaskControllerRetryTest {

    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private ServerAppIdProvider serverAppIdProvider;
    private DownloadTaskController controller;

    @Before
    public void setUp() {
        AdminProperty adminProperty = new AdminProperty();
        adminProperty.setAdminUsername("admin");
        controller = new DownloadTaskController(downloadStorageService, serverAppIdProvider, adminProperty);
    }

    @Test
    public void retry_okWhenClaimSucceeds() {
        when(downloadStorageService.retry(4L, "admin")).thenReturn(true);
        RetryDownloadTaskRequest request = new RetryDownloadTaskRequest();
        request.setRegisterId(4L);

        ResponseResult<?> result = controller.retry(request);

        assertTrue(result.isSuccess());
        verify(downloadStorageService).retry(4L, "admin");
    }

    @Test
    public void retry_failWhenClaimRejected() {
        when(downloadStorageService.retry(4L, "admin")).thenReturn(false);
        RetryDownloadTaskRequest request = new RetryDownloadTaskRequest();
        request.setRegisterId(4L);

        ResponseResult<?> result = controller.retry(request);

        assertFalse(result.isSuccess());
        assertEquals("01", result.getCode());
    }
}
