package me.younian.ssoclient.filter;

import me.younian.ssoclient.auth.AuthInterface;
import me.younian.ssoclient.config.ConfigLoader;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthRequireFilter implements Filter {

    private AuthInterface authInterface;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            System.out.println("AuthRequireFilter init ...");
            ConfigLoader.init();

            Class<?> clazz = Class.forName(ConfigLoader.authInterfaceImplClass);
            if (clazz == null) {
                System.out.println("authInterfaceImplClass not found!");
                throw new ServletException("authInterfaceImplClass not found!");
            }
            authInterface = (AuthInterface) clazz.newInstance();
            if (authInterface == null) {
                System.out.println("authInterface init failed!");
                throw new ServletException("authInterface init failed!");
            } else {
                System.out.println("authInterfaceImplClass:" + authInterface.getClass().getName() + " init success !");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        if (req.getRequestURI().equals(ConfigLoader.logoutUrl)) {

            if (req.getParameter("from") == null || !((String) req.getParameter("from")).equals("sso-server")) {
                String token = authInterface.getToken(req);
                System.out.println("received logout request from user, process logout to sso-server");
                boolean success = authInterface.sendLogoutRequest(token);
            } else {
                String token = req.getParameter("token");
                authInterface.logout(req, res, token);
                System.out.println("received logout request from sso-server");
            }

            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (authInterface.getToken(req) != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 请求附带token参数
        String token = req.getParameter("token");
        if (token != null) {
            // 去sso认证中心校验token
            String basePath = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/";
            boolean verifyResult = authInterface.authCheck( req, res,token, basePath + ConfigLoader.logoutUrl);
            if (verifyResult) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        //跳转至sso认证中心
        String url = UriComponentsBuilder.fromUriString(ConfigLoader.redirectUrl)
                .queryParam("callback", req.getRequestURL())
                //加time是为了让浏览器重新请求
                .queryParam("time", new Date().getTime())
                .build().toString();
        res.sendRedirect(url);
    }

    @Override
    public void destroy() {

    }
}
