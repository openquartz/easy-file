package com.openquartz.easyfile.admin.controller;

import com.openquartz.easyfile.admin.controller.annotation.Auth;
import com.openquartz.easyfile.admin.model.request.LoginRequest;
import com.openquartz.easyfile.admin.service.LoginService;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * index controller
 *
 * @author svnee
 * @since 1.2.0
 */
@Controller
@RequestMapping("/easyfile-ui")
public class IndexController {

    private final LoginService loginService;

    public IndexController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Auth
    @RequestMapping("/")
    public String index() {
        return "forward:/easyfile-ui/index.html";
    }

    @RequestMapping("/toLogin")
    public String toLogin(HttpServletRequest request, HttpServletResponse response) {
        if (loginService.ifLogin(request, response) != null) {
            return "redirect:/easyfile-ui/";
        }
        return "forward:/easyfile-ui/index.html";
    }

    @ResponseBody
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<String> loginDo(@RequestBody @Valid LoginRequest request,
        HttpServletResponse response) {
        boolean isRememberMe = StringUtils.isNotBlank(request.getRememberMe()) && Objects
            .equals(Constants.SWITCH_ON, request.getRememberMe());
        return loginService.login(response, request.getUsername(), request.getPassword(), isRememberMe);
    }

    @Auth
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        loginService.logout(request, response);
        return "redirect:/easyfile-ui/toLogin";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

}
