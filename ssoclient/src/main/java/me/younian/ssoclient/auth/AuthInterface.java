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
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public abstract class AuthInterface {

    public abstract String getToken(HttpServletRequest request);

    public abstract boolean login(HttpServletRequest request, HttpServletResponse response, String token);

    public abstract void logout(HttpServletRequest request, HttpServletResponse response, String token) throws IOException;

    public final boolean authCheck(HttpServletRequest request, HttpServletResponse response, String token, String logoutUrl) {
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
            CloseableHttpResponse res = httpclient.execute(httppost);
            try {
                HttpEntity entity = res.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("authCheck content " + content);
                    JSONObject object = (JSONObject) JSONObject.parse(content);
                    boolean auth = Boolean.parseBoolean(object.getString("auth"));
                    if (auth) {
                        login(request, response, token);
                    }
                    return auth;
                }
            } finally {
                res.close();
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

    public final boolean sendLogoutRequest(String token) throws IOException {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
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
}
