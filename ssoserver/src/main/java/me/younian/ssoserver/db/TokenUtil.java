package me.younian.ssoserver.db;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class TokenUtil {
    private final Map<String, TokenHolder> tokenMap = new HashMap<>();

    public String getCurrentToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("token");
    }

    public boolean addToken(String token, HttpServletRequest request) {
        request.getSession().setAttribute("token", token);
        tokenMap.put(token, new TokenHolder(request.getSession()));
        return true;
    }

    public boolean removeToken(String token) {
        boolean contains = tokenMap.containsKey(token);
        if (contains) {
            //注销Server
            HttpSession session = tokenMap.get(token).getSession();
            if (session != null) {
                session.invalidate();
            }
            //注销其他系统
            sendLogoutRequest(token);
            tokenMap.remove(token);
        }
        return true;
    }

    public boolean checkToken(String token, String logoutUrl) {
        boolean contains = tokenMap.containsKey(token);
        if (contains) {
            //存储子系统注销地址
            tokenMap.get(token).getLogoutUrls().add(logoutUrl);
        }
        return contains;
    }

    private void sendLogoutRequest(String token) {
        for (String url : tokenMap.get(token).getLogoutUrls()) {
            // 创建默认的httpClient实例.
            CloseableHttpClient httpclient = HttpClients.createDefault();
            // 创建httppost
            HttpPost httppost = new HttpPost(url);
            // 创建参数队列
            List formparams = new ArrayList();
            formparams.add(new BasicNameValuePair("token", token));
            formparams.add(new BasicNameValuePair("from", "sso-server"));
            UrlEncodedFormEntity uefEntity;
            try {
                uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httppost.setEntity(uefEntity);
                System.out.println("executing sendLogoutRequest request " + httppost.getURI());
                CloseableHttpResponse response = httpclient.execute(httppost);
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String content = EntityUtils.toString(entity, "UTF-8");
                        System.out.println("sendLogoutRequest content " + content);
                    }
                } finally {
                    response.close();
                }
            } catch (ClientProtocolException | UnsupportedEncodingException e) {
                e.printStackTrace();
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
        }
    }

    private class TokenHolder {
        private HttpSession session;
        private Set<String> logoutUrls;

        public TokenHolder(HttpSession s) {
            this.session = s;
            this.logoutUrls = new HashSet<>();
        }

        public HttpSession getSession() {
            return session;
        }

        public Set<String> getLogoutUrls() {
            return logoutUrls;
        }
    }
}
