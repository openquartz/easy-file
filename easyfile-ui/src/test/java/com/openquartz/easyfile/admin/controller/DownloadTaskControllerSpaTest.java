package com.openquartz.easyfile.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.admin.model.response.AdminUiConfig;
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
public class DownloadTaskControllerSpaTest {

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
    public void get_redirectsToSpaHome() {
        assertEquals("redirect:/easyfile-ui/", controller.get());
    }

    @Test
    public void uiConfig_returnsShowAppIdTrue() {
        when(serverAppIdProvider.isShow()).thenReturn(true);

        ResponseResult<AdminUiConfig> result = controller.uiConfig();

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isShowAppId());
    }

    @Test
    public void uiConfig_returnsShowAppIdFalse() {
        when(serverAppIdProvider.isShow()).thenReturn(false);

        ResponseResult<AdminUiConfig> result = controller.uiConfig();

        assertTrue(result.isSuccess());
        assertFalse(result.getData().isShowAppId());
    }
}
