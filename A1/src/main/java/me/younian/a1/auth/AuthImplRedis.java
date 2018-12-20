package me.younian.a1.auth;

import me.younian.ssoclient.auth.AuthInterface;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthImplRedis extends AuthInterface {
    private static ShardedJedisPool shardedJedisPool = null;
    private CookieUtil cookieUtil = new CookieUtil();

    public AuthImplRedis() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);

        JedisShardInfo jedisShardInfo = new JedisShardInfo("localhost", 6379, 0);
        List shards = new ArrayList();
        shards.add(jedisShardInfo);
        shardedJedisPool = new ShardedJedisPool(jedisPoolConfig, shards);
        System.out.println("shardedJedisPool init ...");
    }

    @Override
    public String getToken(HttpServletRequest request) {
        String token = cookieUtil.getCookie(request, "token");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        ShardedJedis jedis = null;
        try {
            jedis = shardedJedisPool.getResource();
            return jedis.get(token);
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean login(HttpServletRequest request, HttpServletResponse response, String token) {
        System.out.println("login ...");
        cookieUtil.addCookie(response, "token", token);
        ShardedJedis jedis = null;
        try {
            jedis = shardedJedisPool.getResource();
            jedis.set(token, token);
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
        ShardedJedis jedis = null;
        try {
            jedis = shardedJedisPool.getResource();
            jedis.del(token);
            cookieUtil.delCookie(response, "uuid");
        } catch (Exception e) {
            if (jedis != null) {
                jedis.close();
            }
            e.printStackTrace();
        }
    }

    private class CookieUtil {
        public String getCookie(HttpServletRequest request, String key) {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }

        public void addCookie(HttpServletResponse response, String name, String value) {
            if (!StringUtils.isEmpty(name)) {
                Cookie cookie = new Cookie(name, value);
                response.addCookie(cookie);
            }
        }

        public void delCookie(HttpServletResponse response, String name) {
            if (!StringUtils.isEmpty(name)) {
                Cookie cookie = new Cookie(name, null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
    }
}
