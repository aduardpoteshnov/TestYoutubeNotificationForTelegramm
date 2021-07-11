package com.youtube.ishtwar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PubSubRegistration {
    String callbackURI = "https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCN4FNK7oAe2Bmaw7Oi5blog";

    public void subscribe() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("https://pubsubhubbub.appspot.com/subscribe");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> subRequest = new ArrayList<>();
            subRequest.add(new BasicNameValuePair("hub.callback", "https://test-yt-ishtvar.herokuapp.com/"));
            subRequest.add(new BasicNameValuePair("hub.topic", callbackURI));
            subRequest.add(new BasicNameValuePair("hub.verify", "async"));
            subRequest.add(new BasicNameValuePair("hub.mode", "subscribe"));
            subRequest.add(new BasicNameValuePair("hub.verify_token", ""));
            subRequest.add(new BasicNameValuePair("hub.secret", ""));
            subRequest.add(new BasicNameValuePair("hub.lease_seconds", "432000")); //value in seconds 432000 equals to 5days

            httpPost.setEntity(new UrlEncodedFormEntity(subRequest));

            System.out.println("Executing request " + httpPost.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = this::getString;
            String responseBody = httpclient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("________________________________________");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getString(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        System.out.println("Response status is: " + status);
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}
