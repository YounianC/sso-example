package me.younian.a1.auth;

import me.younian.ssoclient.auth.AuthInterface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthImplSession extends AuthInterface {
    private static Map<String, HttpSession> sessionMap = new HashMap<>();

    @Override
    public String getToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        System.out.println("AuthImplSession getToken....");
        return (String) session.getAttribute("isLogin");
    }

    @Override
    public boolean login(HttpServletRequest request, HttpServletResponse response, String token) {
        request.getSession().setAttribute("isLogin", token);
        System.out.println("AuthImplSession login....");
        sessionMap.put(token, request.getSession());
        return true;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
        HttpSession session = sessionMap.get(token);
        if (session != null) {
            session.invalidate();
        }
    }
}
