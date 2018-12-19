package me.younian.ssoclient.auth;

import com.alibaba.fastjson.JSONObject;
import me.younian.ssoclient.config.ConfigLoader;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthBean {

    private static Map<String, HttpSession> sessionMap = new HashMap<>();

    public static String getCurrentToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (String) session.getAttribute("isLogin");
    }

    public static boolean authCheck(String token, String logoutUrl, HttpServletRequest request) {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(ConfigLoader.authCheckUrl);
        // 创建参数队列
        List formparams = new ArrayList();
        formparams.add(new BasicNameValuePair("token", token));
        formparams.add(new BasicNameValuePair("logoutUrl", logoutUrl));
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing authCheck request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("authCheck content " + content);
                    JSONObject object = (JSONObject) JSONObject.parse(content);
                    boolean auth = Boolean.parseBoolean(object.getString("auth"));
                    if (auth) {
                        request.getSession().setAttribute("isLogin", token);
                        sessionMap.put(token, request.getSession());
                    }
                    return auth;
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean logoutForUser(String token) throws IOException {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(ConfigLoader.authLogoutUrl);
        // 创建参数队列
        List formparams = new ArrayList();
        formparams.add(new BasicNameValuePair("token", token));
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing logout request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("logout content " + content);
                    JSONObject object = (JSONObject) JSONObject.parse(content);
                    return Boolean.parseBoolean(object.getString("success"));
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void logoutForServer(String token) throws IOException {
        HttpSession session = sessionMap.get(token);
        if (session != null) {
            session.invalidate();
        }
    }
}
