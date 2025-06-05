package com.mcp.utils;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Get_cookies {

    public static String getc() {
        String cookies = null;
        try {
            URL url = new URL("http://dynamic.12306.cn/otn/board/init");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setInstanceFollowRedirects(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36");
            Map<String, List<String>> m = con.getHeaderFields();
            Set<String> s = m.keySet();
            StringBuffer sb = new StringBuffer();
            if (s.contains("Set-Cookie")) {
                List<String> ls = m.get("Set-Cookie");
                for (int i = 0; i < ls.size(); i++) {
                    String ss = ls.get(i).substring(0,
                            ls.get(i).indexOf(";") + 1);
                    sb.append(ss);
                }
            }
            String tt = sb.toString();
            if (tt != null) {
                cookies = tt.substring(0, tt.length() - 1);
            } else {
                cookies = null;
                System.out.println("cookies is null:");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cookies;
    }

    public static void main(String[] args) {
        getc();
    }
}