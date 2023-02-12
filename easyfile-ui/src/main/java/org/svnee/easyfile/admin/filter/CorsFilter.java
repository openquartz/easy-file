//package org.svnee.easyfile.admin.filter;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * CorsFilter
// * @author svnee
// */
//@Component
//@WebFilter(filterName = "CorsFilter",urlPatterns = "/*")
//public class CorsFilter implements Filter {
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
//        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
//        httpServletResponse.setStatus(200);
//        httpServletResponse.setContentType("text/plain;charset=utf-8");
//        httpServletResponse.setCharacterEncoding("utf-8");
//        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
//        httpServletResponse.setHeader("Access-Control-Max-Age", "0");
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.setHeader("XDdomainRequestAllowed","1");
//        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
//            return;
//        }
//        filterChain.doFilter(servletRequest,servletResponse);
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//}
