package com.demo.utils.http;

import java.util.List;
import java.util.Map;

/**
 * 用于存储Http请求的返回数据
 * @author lijy
 */
public class HttpResult {

    /**
     * http响应码
     */
    private int code;
    /**
     * http响应头
     */
    private Map<String,List<String>> headers;
    /**
     * http响应体
     */
    private String body;

    /**
     * 当前请求结果对应的httpurl地址
     */
    private String httpUrl;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }
}
