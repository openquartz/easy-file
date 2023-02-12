package org.svnee.easyfile.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
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
    @PostMapping("/login")
    public ResponseResult<String> loginDo(HttpServletRequest request, HttpServletResponse response, String username,
        String password, String rememberMe) {
        boolean isRememberMe = StringUtils.isNotBlank(rememberMe) && Objects.equals(Constants.SWITCH_ON, rememberMe);
        return loginService.login(response, username, password, isRememberMe);
    }

    @ResponseBody
    @PostMapping("/logout")
    public ResponseResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginService.logout(request, response);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

}
