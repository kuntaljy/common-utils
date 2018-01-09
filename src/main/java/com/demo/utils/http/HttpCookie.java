package com.demo.utils.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类用保存每一个cookie的值及其相关属性，
 * TODO: 还需要添加超时属性
 * @author lijy
 */
public class HttpCookie {
    private String name;
    private String value;
    private String domain;
    private String path;

    private HttpCookie(){}

    /**
     * 用于匹配cookie字符串的正则表达式
     */
    private static Pattern cookiePattern = Pattern.compile("^([^=]+)=([^;]*)(?:;\\s*)(?:[p|P]ath=([^;]*))?");

    /**
     * 构造cookie对象的工厂方法
     * @param httpUrl
     * @param cookieString
     * @return
     */
    public static HttpCookie create(String httpUrl, String cookieString){
        HttpCookie cookie = new HttpCookie();
        try {
            URL url = new URL(httpUrl);
            if(url.getPort()>0) {
                cookie.domain = String.format("%s://%s:%s", url.getProtocol(), url.getHost(), url.getPort());
            }else {
                cookie.domain = String.format("%s://%s", url.getProtocol(), url.getHost());
            }
            Matcher m = cookiePattern.matcher(cookieString);
            if(m.find()){
                cookie.setName(m.group(1));
                cookie.setValue(m.group(2));
                cookie.setPath(m.group(3)==null?"/":m.group(3));
            }
        } catch (MalformedURLException e) {
            cookie = null;
        }
        return cookie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
