package me.younian.ssoserver;

import com.alibaba.fastjson.JSONObject;
import me.younian.ssoserver.db.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private TokenUtil tokenUtil;

    @RequestMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        String callback = request.getParameter("callback");
        System.out.println("authServer: try login, callback:" + callback);

        String token = tokenUtil.getCurrentToken(request);
        if (token != null) {
            try {
                System.out.println("authServer: already login, callback:" + callback);
                String url = UriComponentsBuilder.fromUriString(callback)
                        .queryParam("token", token)
                        .build().toString();
                response.sendRedirect(url);
                return "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("authServer: require login, callback:" + callback);
        }
        return "login/login";
    }

    @RequestMapping("/loginSubmit")
    public String loginSubmit(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String callback = request.getParameter("callback");

        try {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(callback)) {

                String url = UriComponentsBuilder.fromUriString("/login/login.html")
                        .queryParam("msg", "invalid param")
                        .queryParam("callback", callback)
                        .build().toString();
                response.sendRedirect(url);
                return "";
            }


            if (username.equals("admin") && password.equals("123456")) {
                String token = UUID.randomUUID().toString();

                tokenUtil.addToken(token, request);
                System.out.println("authServer: addToken:" + token);

                String url = UriComponentsBuilder.fromUriString(callback)
                        .queryParam("token", token)
                        .build().toString();

                response.sendRedirect(url);
                return "";
            }

            String url = UriComponentsBuilder.fromUriString("/WEB-INF/views/login/login.html")
                    .queryParam("msg", "invalid username or password")
                    .queryParam("callback", callback)
                    .build().toString();

            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @ResponseBody
    @RequestMapping("/authCheck")
    public JSONObject authCheck(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getParameter("token");
        String logoutUrl = request.getParameter("logoutUrl");
        System.out.println("authServer: authCheck:" + token);
        JSONObject object = new JSONObject();
        if (StringUtils.isEmpty(token)) {
            object.put("success", false);
            object.put("auth", false);
            object.put("msg", "invalid param");
            return object;
        }
        object.put("success", true);
        if (tokenUtil.checkToken(token, logoutUrl)) {
            object.put("auth", true);
            object.put("msg", "success");
        } else {
            object.put("auth", false);
            object.put("msg", "token not valid");
        }
        return object;
    }

    @ResponseBody
    @RequestMapping("/logout")
    public JSONObject logout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getParameter("token");
        System.out.println("authServer: logout:" + token);
        JSONObject object = new JSONObject();
        if (StringUtils.isEmpty(token)) {
            object.put("success", false);
            object.put("msg", "invalid param");
            return object;
        }
        tokenUtil.removeToken(token);
        object.put("success", true);
        object.put("msg", "success");
        return object;
    }
}
