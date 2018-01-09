package com.demo.utils.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * 用于管理所有的cookie内容
 * @author lijy
 */
public class CookieManager {

    //存储所有被管理的cookies，key为域名，value为cookie值
    private Map<String,List<HttpCookie>> allCookies = new HashMap<String,List<HttpCookie>>();

    /**
     * 清空cookie
     */
    public void clear(){
        allCookies.clear();
    }

    /**
     * 添加cookie存储
     * @param cookie
     * @throws IllegalArgumentException
     */
    public void addCookie(HttpCookie cookie) throws IllegalArgumentException{
        if(cookie.getDomain() == null || cookie.getDomain().length() == 0){
            throw new IllegalArgumentException("Cookie对象的Domain属性不能为空");
        }
        List<HttpCookie> cookieList = allCookies.get(cookie.getDomain());
        if(cookieList==null){
            cookieList = new LinkedList<HttpCookie>();
        }

        //如果已经存在同名cookie则替换
        for(HttpCookie item:cookieList){
            if(item.getName() == cookie.getName()){
                item = cookie;
                return;
            }
        }
        //不存在同名cookie则添加
        cookieList.add(cookie);
        allCookies.put(cookie.getDomain(),cookieList);
    }

    /**
     * 获取与httpUrl匹配的cookie值,先匹配domain，在匹配path
     * @param httpUrl
     */
    public List<HttpCookie> getCookie(String httpUrl){
        //1.先获取域下面所有的cookie
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        String domain = String.format("%s://%s:%s",url.getProtocol(), url.getHost(), url.getPort());
        List<HttpCookie> cookieList = allCookies.get(domain);
        //2.从该域下面所有cookie中获取path匹配的项
        List<HttpCookie> result = null;
        if(cookieList != null){
            result = new LinkedList<HttpCookie>();
            for(HttpCookie item:cookieList){
                if(url.getFile().startsWith(item.getPath())){
                    result.add(item);
                }
            }
        }
        return result;
    }
}
