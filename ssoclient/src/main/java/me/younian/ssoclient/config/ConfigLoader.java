package me.younian.ssoclient.config;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ConfigLoader {

    public static String applicationName = "";
    public static String redirectUrl = "";
    public static String authCheckUrl = "";
    public static String authLogoutUrl = "";

    public static String logoutUrl = "";

    public static String authInterfaceImplClass = "";

    public static void init() throws Exception {
        try {
            URL url = new URL("classpath:sso-config.properties");
            InputStream inStream = url.openStream();
            Properties prop = new Properties();
            prop.load(inStream);
            System.out.println("init sso-config.properties: " + prop.toString());
            if (prop.entrySet().size() == 0) {
                System.out.println("!!! init sso-config.properties is empty ");
                throw new FileNotFoundException("sso-config.properties not found");
            }

            String server = prop.getProperty("ServerUrl");
            redirectUrl = server + "/login";
            authCheckUrl = server + "/authCheck";
            authLogoutUrl = server + "/logout";

            applicationName = prop.getProperty("ApplicaitonName");
            logoutUrl = prop.getProperty("LogoutUrl");

            authInterfaceImplClass = prop.getProperty("AuthInterfaceImplClass");
        } catch (Exception e) {
            throw e;
        }
    }
}
