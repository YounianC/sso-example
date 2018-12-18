package me.younian.ssoclient.filter;

import me.younian.ssoclient.auth.AuthBean;
import me.younian.ssoclient.config.ConfigLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class AuthRequireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        HttpSession session = req.getSession();

        if (session.getAttribute("isLogin") != null && (boolean) session.getAttribute("isLogin")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 请求附带token参数
        String token = req.getParameter("token");
        if (token != null) {
            // 去sso认证中心校验token
            boolean verifyResult = AuthBean.authCheck(token);
            if (verifyResult) {
                System.out.println("client session.setAttribute.login = true  sessionId:" + session.getId());
                session.setAttribute("isLogin", true);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        //跳转至sso认证中心
        String url = UriComponentsBuilder.fromUriString(ConfigLoader.redirectUrl)
                .queryParam("callback", req.getRequestURL())
                .build().toString();
        res.sendRedirect(url);
    }

    @Override
    public void destroy() {

    }
}
