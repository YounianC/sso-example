package me.younian.ssoclient.config;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@Component
public class ConfigLoader {

    public static String applicationName = "";
    public static String redirectUrl = "";
    public static String authCheckUrl = "";

    static {
        try {
            URL url = new URL("classpath:/sso-config.properties");
            InputStream inStream = url.openStream();

            Properties prop = new Properties();

            System.out.println(prop.toString());

            prop.load(inStream);
            String server = prop.getProperty("ServerUrl");
            redirectUrl = server + "/login";
            authCheckUrl = server + "/authCheck";
            applicationName = prop.getProperty("ApplicaitonName");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
