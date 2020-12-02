package com.qcloud;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.qcloud.COSSignerConstants.*;

public class AuthorizationGenerate {

    private static Set<String> needSignedHeaderSet = new HashSet<>();
    private int localTimeDelta = 0;
    //默认签名有效时间一小时
    private Date expiredTime = new Date(System.currentTimeMillis() + 3600 * 1000);

    static {
        needSignedHeaderSet.add("host");
        needSignedHeaderSet.add("content-type");
        needSignedHeaderSet.add("content-md5");
        needSignedHeaderSet.add("content-disposition");
        needSignedHeaderSet.add("content-encoding");
        needSignedHeaderSet.add("content-length");
        needSignedHeaderSet.add("transfer-encoding");
        needSignedHeaderSet.add("range");
    }

    public String buildAuthorizationStr(HttpMethodName methodName, String uri,
                                        COSCredentials cred, Date expiredTime) throws UnsupportedEncodingException {
        return buildAuthorizationStr(methodName, uri, new HashMap<String, String>(),
                new HashMap<String, String>(), cred, expiredTime);
    }

    public String buildAuthorizationStr(HttpMethodName methodName, String uri,
                                        COSCredentials cred) throws UnsupportedEncodingException {
        return buildAuthorizationStr(methodName, uri, new HashMap<String, String>(),
                new HashMap<String, String>(), cred, expiredTime);
    }

    public String buildPostObjectSignature(String secretKey, String keyTime, String policy) {
        String signKey = HmacUtils.hmacSha1Hex(secretKey, keyTime);
        String stringToSign = DigestUtils.sha1Hex(policy);
        return HmacUtils.hmacSha1Hex(signKey, stringToSign);
    }

    public String buildAuthorizationStr(HttpMethodName methodName, String uri,
                                        Map<String, String> headerMap, Map<String, String> paramMap, COSCredentials cred,
                                        Date expiredTime) throws UnsupportedEncodingException {
        Date startTime = new Date();
        return buildAuthorizationStr(methodName, uri, headerMap, paramMap,
                cred, startTime, expiredTime);
    }

    public String buildAuthorizationStr(HttpMethodName methodName, String uri,
                                        Map<String, String> headerMap, Map<String, String> paramMap, COSCredentials cred) throws UnsupportedEncodingException {
        Date startTime = new Date();
        return buildAuthorizationStr(methodName, uri, headerMap, paramMap,
                cred, startTime, expiredTime);
    }

    public String buildAuthorizationStr(HttpMethodName methodName, String uri,
                                        Map<String, String> headerMap, Map<String, String> paramMap, COSCredentials cred,
                                        Date startTime, Date expiredTime) throws UnsupportedEncodingException {
        Map<String, String> signHeaders = buildSignHeaders(headerMap);
        // 签名中的参数和http 头部 都要进行字符串排序
        TreeMap<String, String> sortedSignHeaders = new TreeMap<>();
        TreeMap<String, String> sortedParams = new TreeMap<>();

        sortedSignHeaders.putAll(signHeaders);
        sortedParams.putAll(paramMap);

        String qHeaderListStr = buildSignMemberStr(sortedSignHeaders);
        String qUrlParamListStr = buildSignMemberStr(sortedParams);
        String qKeyTimeStr, qSignTimeStr;
        qKeyTimeStr = qSignTimeStr = buildTimeStr(startTime, expiredTime);
        String signKey = HmacUtils.hmacSha1Hex(cred.getCOSSecretKey(), qKeyTimeStr);
        String formatMethod = methodName.toString().toLowerCase();
        String formatUri = uri;
        String formatParameters = formatMapToStr(sortedParams);
        String formatHeaders = formatMapToStr(sortedSignHeaders);

        String formatStr = new StringBuilder().append(formatMethod).append(LINE_SEPARATOR)
                .append(formatUri).append(LINE_SEPARATOR).append(formatParameters)
                .append(LINE_SEPARATOR).append(formatHeaders).append(LINE_SEPARATOR).toString();
        String hashFormatStr = DigestUtils.sha1Hex(formatStr);
        String stringToSign = new StringBuilder().append(Q_SIGN_ALGORITHM_VALUE)
                .append(LINE_SEPARATOR).append(qSignTimeStr).append(LINE_SEPARATOR)
                .append(hashFormatStr).append(LINE_SEPARATOR).toString();
        String signature = HmacUtils.hmacSha1Hex(signKey, stringToSign);

        String authoriationStr = new StringBuilder().append(Q_SIGN_ALGORITHM_KEY).append("=")
                .append(Q_SIGN_ALGORITHM_VALUE).append("&").append(Q_AK).append("=")
                .append(cred.getCOSAccessKeyId()).append("&").append(Q_SIGN_TIME).append("=")
                .append(qSignTimeStr).append("&").append(Q_KEY_TIME).append("=").append(qKeyTimeStr)
                .append("&").append(Q_HEADER_LIST).append("=").append(qHeaderListStr).append("&")
                .append(Q_URL_PARAM_LIST).append("=").append(qUrlParamListStr).append("&")
                .append(Q_SIGNATURE).append("=").append(signature).toString();
        return authoriationStr;
    }

    public boolean needSignedHeader(String header) {
        return needSignedHeaderSet.contains(header) || header.startsWith("x-cos-");
    }

    private Map<String, String> buildSignHeaders(Map<String, String> originHeaders) {
        Map<String, String> signHeaders = new HashMap<>();
        for (Map.Entry<String, String> headerEntry : originHeaders.entrySet()) {
            String key = headerEntry.getKey().toLowerCase();
            if (needSignedHeader(key)) {
                String value = headerEntry.getValue();
                signHeaders.put(key, value);
            }
        }
        return signHeaders;
    }

    private String buildSignMemberStr(Map<String, String> signHeaders) {
        StringBuilder strBuilder = new StringBuilder();
        boolean seenOne = false;
        for (String key : signHeaders.keySet()) {
            if (!seenOne) {
                seenOne = true;
            } else {
                strBuilder.append(";");
            }
            strBuilder.append(key.toLowerCase());
        }
        return strBuilder.toString();
    }

    private String formatMapToStr(Map<String, String> kVMap) throws UnsupportedEncodingException {
        StringBuilder strBuilder = new StringBuilder();
        boolean seeOne = false;
        for (Map.Entry<String, String> entry : kVMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String lowerKey = key.toLowerCase();
            String encodeKey = UrlEncoderUtils.encode(lowerKey);
            String encodedValue = "";
            if (value != null) {
                encodedValue = UrlEncoderUtils.encode(value);
            }
            if (!seeOne) {
                seeOne = true;
            } else {
                strBuilder.append("&");
            }
            strBuilder.append(encodeKey).append("=").append(encodedValue);
        }
        return strBuilder.toString();
    }

    private String buildTimeStr(Date startTime, Date endTime) {
        StringBuilder strBuilder = new StringBuilder();
        long startTimestamp = startTime.getTime() / 1000 + localTimeDelta;
        long endTimestamp = endTime.getTime() / 1000 + localTimeDelta;
        strBuilder.append(startTimestamp).append(";").append(endTimestamp);
        return strBuilder.toString();
    }

}


