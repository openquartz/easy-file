package org.svnee.easyfile.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.svnee.easyfile.admin.controller.annotation.Auth;
import org.svnee.easyfile.admin.model.request.LoginRequest;
import org.svnee.easyfile.admin.service.LoginService;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * index controller
 *
 * @author svnee
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
    public String index(Model model) {

        return "index";
    }

    @RequestMapping("/toLogin")
    public ModelAndView toLogin(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) {
        if (loginService.ifLogin(request, response) != null) {
            modelAndView.setView(new RedirectView("/", true, false));
            return modelAndView;
        }
        return new ModelAndView("login");
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
        return "login";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

}
