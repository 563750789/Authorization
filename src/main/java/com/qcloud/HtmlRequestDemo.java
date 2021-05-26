package com.qcloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class HtmlRequestDemo {
    public static void main(String[] args) {
        HtmlRequestDemo htmlRequestDemo = new HtmlRequestDemo();
        String previewUrl = htmlRequestDemo.getPreviewUrl("htmltest.pptx");
        System.out.println(previewUrl);
    }

    private String getPreviewUrl(String filePath) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("http://markjrzhang-1251704708.cos.ap-chongqing.myqcloud.com/"+filePath);
            List<NameValuePair> list = new LinkedList<>();
            list.add(new BasicNameValuePair("ci-process", "doc-preview"));
            list.add(new BasicNameValuePair("dstType", "html"));
            list.add(new BasicNameValuePair("weboffice_url", "1"));
            uriBuilder.setParameters(list);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            String responseBody = httpClient.execute(httpGet, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    // ... handle unsuccessful request
                }
                HttpEntity entity = httpResponse.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            });
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            return jsonObject.getString("PreviewUrl");
        } catch (IOException | URISyntaxException e) {
            // ... handle IO exception
        }
        return "false";
    }
}
