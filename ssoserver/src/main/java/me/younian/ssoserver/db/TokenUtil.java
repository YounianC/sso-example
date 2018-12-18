package me.younian.ssoserver.db;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtil {
    private static final Map<String, String> tokenMap = new HashMap<>();

    public static boolean addToken(String token) {
        tokenMap.put(token, token);
        return true;
    }

    public static boolean removeToken(String token) {
        tokenMap.remove(token);
        return true;
    }

    public static boolean checkToken(String token) {
        return tokenMap.containsKey(token);
    }
}
