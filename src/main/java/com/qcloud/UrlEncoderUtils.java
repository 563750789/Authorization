/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.

 * According to cos feature, we modify some class，comment, field name, etc.
 */
package com.qcloud;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlEncoderUtils {

    private static final String PATH_DELIMITER = "/";
    private static final String ENCODE_DELIMITER = "%2F";

    public static String encode(String originUrl) throws UnsupportedEncodingException {
        return URLEncoder.encode(originUrl, "utf-8").replace("+", "%20").replace("*", "%2A")
                .replace("%7E", "~");
    }

    // encode路径, 不包括分隔符
    public static String encodeEscapeDelimiter(String urlPath) throws UnsupportedEncodingException {
        StringBuilder pathBuilder = new StringBuilder();
        String[] pathSegmentsArr = urlPath.split(PATH_DELIMITER);

        boolean isFirstSegMent = true;
        for (String pathSegment : pathSegmentsArr) {
            if (isFirstSegMent) {
                pathBuilder.append(encode(pathSegment));
                isFirstSegMent = false;
            } else {
                pathBuilder.append(PATH_DELIMITER).append(encode(pathSegment));
            }
        }
        if (urlPath.endsWith(PATH_DELIMITER)) {
            pathBuilder.append(PATH_DELIMITER);
        }
        return pathBuilder.toString();
    }

    // encode url path, replace the continuous slash with %2F except the first slash
    public static String encodeUrlPath(String urlPath) throws UnsupportedEncodingException {
        if (urlPath.length() <= 1) {
            return urlPath;
        }

        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(PATH_DELIMITER);
        int start = 1, end = 1;
        while (end < urlPath.length()) {
            if ('/' == urlPath.charAt(end)) {
                if ('/' == urlPath.charAt(end - 1)) {
                    pathBuilder.append(ENCODE_DELIMITER);
                } else {
                    pathBuilder.append(encode(urlPath.substring(start, end))).append(PATH_DELIMITER);
                }
                start = end + 1;
            }
            end++;
        }
        if (start < end) {
            pathBuilder.append(encode(urlPath.substring(start, end)));
        }
        return pathBuilder.toString();
    }

    /**
     * Decode a string for use in the path of a URL; uses URLDecoder.decode,
     * which decodes a string for use in the query portion of a URL.
     *
     * @param value The value to decode
     * @return The decoded value if parameter is not null, otherwise, null is returned.
     */
    public static String urlDecode(final String value) {
        if (value == null) {
            return null;
        }

        try {
            return URLDecoder.decode(value, "utf-8");

        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
