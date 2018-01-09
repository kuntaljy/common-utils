package com.demo.utils.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 对HttpRequestClient进一步封装，实现了自动登录过程
 * 外部程序直接调用request方法访问接口，不用考虑是否已经登录，以及登录是否已经超期
 * @author Administrator
 */
public class AutoLoginRequestClient {

    private String username;
    private String passWord;
    private String ssoLogin;
    private HttpRequestClient reqClient = new HttpRequestClient();

    public AutoLoginRequestClient(String username, String passWord, String loginUrl){
        this.username = username;
        this.passWord = passWord;
        this.ssoLogin = loginUrl;
    }

    public HttpResult request(String httpUrl, String method, String data, Map<String,String> headers)
            throws NoSuchAlgorithmException, IOException, KeyManagementException {
        HttpResult result = reqClient.request(httpUrl,method,data,headers);
        if(result.getHttpUrl().startsWith(ssoLogin)){
            result = autoLogin(result);
        }
        return result;
    }

    public HttpResult autoLogin(HttpResult loginPage) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        HttpResult result = null;
        //1.获取页面信息
        String html = loginPage.getBody();
        String lt = html.substring(html.indexOf("name=\"lt\" value=\"")+17, html.indexOf("name=\"execution\" value=\""));
        lt = lt.substring(0,lt.indexOf("\" />"));
        String execution = html.substring(html.indexOf("name=\"execution\" value=\"")+24, html.indexOf("name=\"execution\" value=\"")+24+4);
        String _eventId = "submit";
        String submit = "登录";
        //2.组织登录参数
        Map<String,String> data = new HashMap<String,String>();
        data.put("username", username);
        data.put("password", passWord);
        data.put("lt", lt);
        data.put("execution", execution);
        data.put("_eventId", _eventId);
        data.put("submit", submit);
        data.put("proxyUserName", "");
        StringBuilder outputStrBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = URLEncoder.encode(entry.getValue(), "utf-8");
            outputStrBuilder.append(String.format("%s=%s&", key, value));
        }
        String dataStr = outputStrBuilder.toString();
        //执行登录
        result = reqClient.request(loginPage.getHttpUrl(),"POST",dataStr,null);
        return result;
    }
}
