package com.qcloud;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;

public class Demo {
    //默认签名有效时间一小时
    private static Date expiredTime = new Date(System.currentTimeMillis() + 3600 * 1000);

    /**
     * API 密钥是构建腾讯云 API 请求的重要凭证，使用腾讯云 API 可以操作您名下的所有腾讯云资源，
     * 为了您的财产和服务安全，请妥善保存和定期更换密钥
     * 秘钥获取地址：https://console.cloud.tencent.com/cam/capi
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        //输入API秘钥
        COSCredentials credentials = new COSCredentials("secret_id", "secret_key");
        AuthorizationGenerate authorizationGenerate = new AuthorizationGenerate();
        //http请求头信息
        HashMap<String, String> httpHeadMap = new HashMap<>();
        httpHeadMap.put("Host", "markjrzhang-1251704708.ci.ap-chongqing.myqcloud.com");
        //http请求参数信息
        HashMap<String, String> httpParamMap = new HashMap<>();
        httpParamMap.put("bucketName", "markjrzhang-1251704708");
        //请求接口路径
        String uri = "/mediabucket";
        //生成签名串
        String authorization = authorizationGenerate.buildAuthorizationStr(HttpMethodName.GET, uri, httpHeadMap, httpParamMap, credentials, expiredTime);
        //注 使用时放入http请求头中 key：Authorization
        System.out.println(authorization);
    }
}
