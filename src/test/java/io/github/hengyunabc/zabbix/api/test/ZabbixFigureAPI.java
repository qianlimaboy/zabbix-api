package io.github.hengyunabc.zabbix.api.test;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZabbixFigureAPI {

    public static String cookieDownload(String loginURL, String user, String password){
        // 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
        PostMethod postMethod = new PostMethod(loginURL);
        HttpClient httpClient = new HttpClient();

        // 设置登陆时要求的信息，用户名和密码
        NameValuePair[] data = {new NameValuePair("name", "Admin"), new NameValuePair("password", "zabbix"),
                new NameValuePair("enter", "Sign in")};
        postMethod.setRequestBody(data);
        StringBuffer tmpcookies = new StringBuffer();
        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            int statusCode = httpClient.executeMethod(postMethod);

            // 获得登陆后的 Cookie
            Cookie[] cookies = httpClient.getState().getCookies();

            for (Cookie c : cookies) {
                tmpcookies.append(c.toString() + ";");
                System.out.println("cookies = " + c.toString());
            }
            if (statusCode == 302) {//重定向到新的URL
                System.out.println("模拟登录成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpcookies.toString();
    }

    public static InputStream chartDownload(String chartURL, String cookie, int graphid, int period, int width, int height){
        HttpClient httpClient = new HttpClient();
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        try {
                GetMethod getMethod = new GetMethod(chartURL);
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

    public static void main(String[] args) {
        // 登陆 Url
        String loginUrl = "http://47.95.246.35:18118/index.php";
        // 需登陆后访问的 Url
        String dataUrl = "http://47.95.246.35:18118/chart2.php?graphid=8285&width=400&period=3600&height=156";
        HttpClient httpClient = new HttpClient();

        // 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
        PostMethod postMethod = new PostMethod(loginUrl);

        // 设置登陆时要求的信息，用户名和密码
        NameValuePair[] data = {new NameValuePair("name", "Admin"), new NameValuePair("password", "zabbix"),
                                new NameValuePair("enter", "Sign in")};
        postMethod.setRequestBody(data);
        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            int statusCode = httpClient.executeMethod(postMethod);

            // 获得登陆后的 Cookie
            Cookie[] cookies = httpClient.getState().getCookies();
            StringBuffer tmpcookies = new StringBuffer();
            for (Cookie c : cookies) {
                tmpcookies.append(c.toString() + ";");
                System.out.println("cookies = " + c.toString());
            }
            if (statusCode == 302) {//重定向到新的URL
                System.out.println("模拟登录成功");
                // 进行登陆后的操作
                GetMethod getMethod = new GetMethod(dataUrl);
                // 每次访问需授权的网址时需带上前面的 cookie 作为通行证
                getMethod.setRequestHeader("cookie", tmpcookies.toString());
                // 你还可以通过 PostMethod/GetMethod 设置更多的请求后数据
                // 例如，referer 从哪里来的，UA 像搜索引擎都会表名自己是谁，无良搜索引擎除外
//                postMethod.setRequestHeader("Referer", "http://passport.mop.com/");
                postMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
                httpClient.executeMethod(getMethod);
                // 打印出返回数据，检验一下是否成功
                InputStream inputStream = getMethod.getResponseBodyAsStream();
                byte[] tempData = new byte[1024];
                int len = 0;
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream("D:\\test1.png");
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
            } else {
                System.out.println("登录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
