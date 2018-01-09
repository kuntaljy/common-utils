package com.demo.utils.http;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lijy
 */
public class AutoLoginRequestClientTest {

    @Test
    public void TestClient(){
        String username = "admin";
        String passWord = "1";
        String ssoLogin = "http://192.168.200.104:8080/sso/login";
        String reqUrl = "http://192.168.1.82:8087/daprest/dcc/orup/v1/user/currentUser";

        //为每一个用户创建一个自动登录请求客户端,注意不要重复创建
        AutoLoginRequestClient autoLoginRequestClient = new AutoLoginRequestClient(username,passWord,ssoLogin);
        try {

            //然后可以直接请求任何接口，它内部会自动登录
            HttpResult result = autoLoginRequestClient.request(reqUrl,"GET",null,null);
            System.out.println(result.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}