package com.demo.utils.http;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行http请求，并支持302跳转
 * @author lijy
 */
public class HttpRequestClient {

    //默认字符集
    private static final String CHARSET  = "utf-8";
    //默认超时时间15秒钟
    private static final int TIME_OUT = 30000;
    //缓冲区大小
    private static final int BUFFER_SIZE = 4096;
    //每一个HttpRequestClient都有一个自己的cookie管理器
    private CookieManager cookieManager = new CookieManager();

    /**
     * 向httpUrl地址发送单表单请求
     * @param httpUrl   请求地址
     * @param method    http请求方法：Get/Post/Delete/Put等
     * @param headers   添加http头信息，可以为空
     * @param data      添加http提交的数据，可以为空
     * @return  请求返回值：如果状态码为200，则返回请求结果数据,否则触发异常
     * @throws Exception IO错误异常或HTTP状态码以4、5开头而触发的异常
     */
    public HttpResult request(String httpUrl, String method, String data, Map<String,String> headers)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpResult result = null;
        //1.携带cookie信息
        List<HttpCookie> cookieList = cookieManager.getCookie(httpUrl);
        if(cookieList!=null){
            if(headers==null){
                headers = new HashMap<String, String>();
            }
            StringBuilder cookieSB = new StringBuilder();
            for(HttpCookie cookie:cookieList){
                cookieSB.append(cookie.getName() + "=" + cookie.getValue() + ";");
            }
            headers.put("Cookie",cookieSB.toString());
        }
        //2.执行请求
        result = doRequest(httpUrl, method, data, headers);
        //3.记录cookie信息
        List<String> setCookies = result.getHeaders().get("Set-Cookie");
        if(setCookies!=null && setCookies.size()>0){
            for(String item:setCookies) {
                HttpCookie cookie = HttpCookie.create(httpUrl,item);
                cookieManager.addCookie(cookie);
            }
        }
        //4.自动跳转
        if(result.getCode()==302){
            List<String> location = result.getHeaders().get("Location");
            if(location!=null && location.size()>0){
                //location可能是相对地址，也可能是绝对地址
                String tmpUrl = location.get(0);
                String realUrl = null;
                if(tmpUrl.startsWith("http://") || tmpUrl.startsWith("https://")){
                    //如果location是绝对地址则直接使用
                    realUrl = tmpUrl;
                }else{
                    //如果location是相对地址则参考上一次请求地址转换为绝对地址
                    URL referUrl = new URL(result.getHttpUrl());
                    if(referUrl.getPort()>0){
                        if(tmpUrl.startsWith("/")){
                            tmpUrl = tmpUrl.substring(1);
                        }
                        realUrl = String.format("%s://%s:%s/%s", referUrl.getProtocol(), referUrl.getHost(),
                                referUrl.getPort(), tmpUrl);
                    }else{
                        realUrl = String.format("%s://%s/%s", referUrl.getProtocol(), referUrl.getPort(), tmpUrl);
                    }
                }
                result = request(realUrl, "GET", null, null);
            }
        }
        return result;
    }

    /**
     * 向httpUrl地址发送请求</p>
     * @param httpUrl 请求地址
     * @param method http请求方法：Get/Post/Delete/Put等
     * @param requestHeaders 添加http头信息，可以为空
     * @param data 添加http提交的表单数据，可以为空
     * @return 返回http请求结果
     * @throws Exception IO错误异常等
     */
    public static HttpResult doRequest(String httpUrl, String method, String data, Map<String,String> requestHeaders)
            throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpResult httpResult = new HttpResult();
        HttpURLConnection urlConn = null;
        DataOutputStream dos = null;
        BufferedInputStream bis = null;
        OutputStream ops = null;

        try {
            //建立连接
            URL realUrl = new URL(httpUrl);
            urlConn = (HttpURLConnection) realUrl.openConnection();
            //设置参数
            urlConn.setDoInput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod(method.toUpperCase());
            urlConn.setConnectTimeout(TIME_OUT);
            urlConn.setInstanceFollowRedirects(false);
            if(data!=null){
                urlConn.setDoOutput(true);
            }
            if (urlConn instanceof HttpsURLConnection) {
                trust((HttpsURLConnection) urlConn);
            }
            //设置默认header值
            urlConn.setRequestProperty("accept", "*/*");
            urlConn.setRequestProperty("Connection", "keep-alive");
            urlConn.setRequestProperty("charset", CHARSET);
            urlConn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            urlConn.setRequestProperty("content-type","application/x-www-form-urlencoded");
            //设置指定的headers，如果header已经在默认值中设置过了，则会覆盖默认值的设置
            if (requestHeaders != null && requestHeaders.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            //链接
            urlConn.connect();
            //提交请求数据
            if(data != null) {
                dos = new DataOutputStream(urlConn.getOutputStream());
                dos.writeBytes(data);
                dos.flush();
            }
            //记录请求地址
            httpResult.setHttpUrl(httpUrl);
            //接收响应码
            httpResult.setCode(urlConn.getResponseCode());
            //获取响应头
            httpResult.setHeaders(urlConn.getHeaderFields());
            if (HttpURLConnection.HTTP_OK == httpResult.getCode() ||
                    HttpURLConnection.HTTP_PARTIAL == httpResult.getCode()) {
                //若返回的状态码为200或206，表示请求成功，则获取返回值
                ops = new ByteArrayOutputStream();
                int size = 0;
                byte[] bytes = new byte[BUFFER_SIZE];
                bis = new BufferedInputStream(urlConn.getInputStream());
                while ((size = bis.read(bytes)) != -1) {
                    ops.write(bytes, 0, size);
                }
                httpResult.setBody(ops.toString());
            }
        }finally {
            safeClose(dos);
            safeClose(bis);
            safeClose(ops);
            urlConn.disconnect();
        }
        return httpResult;
    }


    /**
     * 构建https请求的证书信息
     * @param httpsConn https链接
     * @throws Exception
     */
    private static void trust(HttpsURLConnection httpsConn) throws NoSuchAlgorithmException, KeyManagementException {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new DistTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        httpsConn.setSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * 安全关闭输入流
     * @param inputStream
     */
    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {}
        }
    }

    /**
     * 安全关闭输出流
     * @param outputStream
     */
    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {}
        }
    }
}
