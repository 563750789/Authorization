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
        COSCredentials credentials = new COSCredentials("secret_id", "secret_key");
        AuthorizationGenerate authorizationGenerate = new AuthorizationGenerate();
        HashMap<String, String> httpHeadMap = new HashMap<>();
        httpHeadMap.put("Host", "markjrzhang-1251704708.ci.ap-chongqing.myqcloud.com");
        HashMap<String, String> httpParamMap = new HashMap<>();
        httpParamMap.put("bucketName", "markjrzhang-1251704708");
        String s = authorizationGenerate.buildAuthorizationStr(HttpMethodName.GET, "/mediabucket", httpHeadMap, httpParamMap, credentials, expiredTime);
        System.out.println(s);
    }
}
