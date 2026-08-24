package com.openquartz.easyfile.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.admin.model.EasyFileAdminUser;
import com.openquartz.easyfile.admin.service.LoginService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexControllerSpaTest {

    @Mock
    private LoginService loginService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private IndexController controller;

    @Before
    public void setUp() {
        controller = new IndexController(loginService);
    }

    @Test
    public void index_forwardsSpa() {
        assertEquals("forward:/easyfile-ui/index.html", controller.index());
    }

    @Test
    public void toLogin_forwardsSpaWhenAnonymous() {
        when(loginService.ifLogin(request, response)).thenReturn(null);

        assertEquals("forward:/easyfile-ui/index.html", controller.toLogin(request, response));
    }

    @Test
    public void toLogin_redirectsHomeWhenAlreadyLoggedIn() {
        when(loginService.ifLogin(request, response)).thenReturn(new EasyFileAdminUser("admin", "admin"));

        assertEquals("redirect:/easyfile-ui/", controller.toLogin(request, response));
    }

    @Test
    public void logout_clearsSessionAndRedirectsToLogin() {
        assertEquals("redirect:/easyfile-ui/toLogin", controller.logout(request, response));
        verify(loginService).logout(request, response);
    }
}
