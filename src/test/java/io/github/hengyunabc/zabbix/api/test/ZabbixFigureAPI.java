package io.github.hengyunabc.zabbix.api.test;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZabbixFigureAPI {

    static Logger logger = LoggerFactory.getLogger(ZabbixFigureAPI.class);

    public static String cookieDownload(String host, String user, String password){
        // 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
        PostMethod postMethod = new PostMethod(host+"/index.php");
        HttpClient httpClient = new HttpClient();

        // 设置登陆时要求的信息，用户名和密码
        NameValuePair[] data = {new NameValuePair("name", user), new NameValuePair("password", password),
                new NameValuePair("enter", "Sign in")};
        postMethod.setRequestBody(data);
        StringBuffer tmpcookies = new StringBuffer();
        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode == 302) {//重定向到新的URL
                // 获得登陆后的 Cookie
                Cookie[] cookies = httpClient.getState().getCookies();

                for (Cookie c : cookies) {
                    tmpcookies.append(c.toString() + ";");
                    logger.info("cookies = {}", c.toString());
                }
                logger.info("模拟登录成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpcookies.toString();
    }

    public static InputStream chartDownload(String host, String cookie, int graphid, int period, int width, int height){
        HttpClient httpClient = new HttpClient();
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        try {
                String params = "graphid=" + graphid + "&period=" + period + "&width=" + width + "&height=" + height;
                GetMethod getMethod = new GetMethod(host + "/chart2.php?" + params);
                // 每次访问需授权的网址时需带上前面的 cookie 作为通行证
                getMethod.setRequestHeader("cookie", cookie);
                getMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
                httpClient.executeMethod(getMethod);
                inputStream = getMethod.getResponseBodyAsStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    @Test
    public void testChart(){
        // zabbix Url
        String host = "http://47.95.246.35:18118";

        String cookie = cookieDownload(host, "Admin", "zabbix");
        InputStream inputStream = chartDownload(host, cookie,8285, 3600,400, 156);

        byte[] tempData = new byte[1024];
        int len = 0;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("D:\\test3.png");
            while ((len = inputStream.read(tempData)) != -1) {
                fileOutputStream.write(tempData, 0, len);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        System.out.println(inputStream);
    }
}
