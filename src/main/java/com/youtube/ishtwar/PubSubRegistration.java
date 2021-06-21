package com.youtube.ishtwar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PubSubRegistration {
    String callbackURI = "";

    public void subscribe() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("https://pubsubhubbub.appspot.com/subscribe");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("hub.callback", "https://test-yt-ishtvar.herokuapp.com/"));
            nvps.add(new BasicNameValuePair("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCN4FNK7oAe2Bmaw7Oi5blog"));
            nvps.add(new BasicNameValuePair("hub.verify", "async"));
            nvps.add(new BasicNameValuePair("hub.mode", "subscribe"));
            nvps.add(new BasicNameValuePair("hub.verify_token", ""));
            nvps.add(new BasicNameValuePair("hub.secret", ""));
            nvps.add(new BasicNameValuePair("hub.lease_seconds", ""));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            System.out.println("Executing request " + httpPost.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    System.out.println("Response status is: " + status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
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

    public void testSubscribe() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("https://pubsubhubbub.appspot.com/subscribe");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("hub.callback", "http://2eea36c703b2.ngrok.io/"));
            nvps.add(new BasicNameValuePair("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCLAu8nIeamvnNJa7SzQAcgg"));
            nvps.add(new BasicNameValuePair("hub.verify", "async"));
            nvps.add(new BasicNameValuePair("hub.mode", "subscribe"));
            nvps.add(new BasicNameValuePair("hub.verify_token", ""));
            nvps.add(new BasicNameValuePair("hub.secret", ""));
            nvps.add(new BasicNameValuePair("hub.lease_seconds", ""));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            System.out.println("Executing request " + httpPost.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    System.out.println("Response status is: " + status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };

            String responseBody = httpclient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
                System.out.println("Subscription to mouse's channel completed!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    public void unsubscribe() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("https://pubsubhubbub.appspot.com/subscribe");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("hub.callback", "https://test-yt-ishtvar.herokuapp.com/"));
            nvps.add(new BasicNameValuePair("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCN4FNK7oAe2Bmaw7Oi5blog"));
            nvps.add(new BasicNameValuePair("hub.verify", "async"));
            nvps.add(new BasicNameValuePair("hub.mode", "unsubscribe"));
            nvps.add(new BasicNameValuePair("hub.verify_token", ""));
            nvps.add(new BasicNameValuePair("hub.secret", ""));
            nvps.add(new BasicNameValuePair("hub.lease_seconds", ""));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            System.out.println("Executing request " + httpPost.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    System.out.println("Response status is: " + status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
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


    /*public void checkSubscribtion(){

    }*/
}
