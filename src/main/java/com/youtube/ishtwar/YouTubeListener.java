package com.youtube.ishtwar;

import com.youtube.ishtwar.db.BotDb;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.StringReader;

import javax.swing.text.html.ListView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class YouTubeListener extends NanoHTTPD {

    private YouTubeNotificationsBot observer;
    private HashMap<String, Integer> sentItems;

    public YouTubeListener(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\b NanoHTTPD Running!");
        sentItems = BotDb.getInstance().getSentItemsList();
    }

    @Override
    public Response serve(IHTTPSession session) {  //Ловим хттп реквест
        if (session.getMethod() == Method.POST) {   //post ожидаем только от ютубчика
            System.out.println("NEW POST RECEIVED " + new Date().getTime());
            try {
                Map<String, String> body = new HashMap<>();
                session.parseBody(body); //вытаскиваем бодик
                System.out.println("Get post body");
                System.out.println("+++++++++++++++++++++==================+++++++++++++++");
                body.forEach((key, value) -> {
                    System.out.println(xmlParser(value));
                    System.out.println("+++++++++++++++++++++==================+++++++++++++++");
                    System.out.println("Second parse body to another hashmap");
                    handleNewlyReceivedVideo(xmlParser(value)); //отправляем xml полученый из бодика и парсим его в еще одну хешмапу
                });
                return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK");
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
        }


        if (session.getMethod() == Method.GET) { //Гет ожидаем только от pubHubSub. Ловим, отвечаем обратно + регаем новую подписку в базе
            System.out.println("New GET received");
            System.out.println(session.getParameters().toString());
            return newFixedLengthResponse(session.getParameters().get("hub.challenge").get(0));
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT,
                "Something went wrong, request not supported");
    }

    private HashMap<String, String> xmlParser(String xml) {
        HashMap<String, String> youtubeRequest = new HashMap<>();

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource source = new InputSource();
            source.setCharacterStream(new StringReader(xml));

            Document doc = docBuilder.parse(source);
            NodeList entry = doc.getElementsByTagName("entry");
            NodeList author = doc.getElementsByTagName("author");

            for (int i = 0; i < entry.getLength(); i++) {
                Element element = (Element) entry.item(i);

                NodeList ytvideoId = element.getElementsByTagName("yt:videoId");
                Element line = (Element) ytvideoId.item(0);
                youtubeRequest.put("videoId", getCharacterDataFromElementXmlParserPart(line));

                NodeList published = element.getElementsByTagName("published");
                line = (Element) published.item(0);
                youtubeRequest.put("published", getCharacterDataFromElementXmlParserPart(line));

                NodeList updated = element.getElementsByTagName("updated");
                line = (Element) updated.item(0);
                youtubeRequest.put("updated", getCharacterDataFromElementXmlParserPart(line));
            }

            for (int i = 0; i < author.getLength(); i++) {
                Element element = (Element) author.item(i);

                NodeList authorName = element.getElementsByTagName("name");
                Element line = (Element) authorName.item(0);
                youtubeRequest.put("channelTitle", getCharacterDataFromElementXmlParserPart(line));

                NodeList uri = element.getElementsByTagName("uri");
                line = (Element) uri.item(0);
                youtubeRequest.put("channelUri", getCharacterDataFromElementXmlParserPart(line));
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return youtubeRequest;
    }

    public static String getCharacterDataFromElementXmlParserPart(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private void handleNewlyReceivedVideo(HashMap<String, String> newVideo) {
        String videoId = newVideo.get("videoId");
        String updated = newVideo.get("updated");
        String published = newVideo.get("published");

        if (videoId == null) {
            System.out.println("Item with videoId = null received. Delivery to TG rejected");
        } else if (sentItems.containsKey(videoId)) {
            if (sentItems.get(videoId) == 0) {
                if (isSendApproved(updated, published, 0)) {
                    System.out.println("New video item received twice. Looks like an error.");
                    System.out.println("This :" + videoId + " should be already added to postpone delivery");
                } else {
                    System.out.println("Item: " + videoId + " received");
                    sendItemToBot(videoId);
                    System.out.println("Item: " + videoId + " send to TG");
                }
            } else if (sentItems.get(videoId) == 1) {
                System.out.println("Received item: " + videoId + " was send to TG already");
                if (isSendApproved(updated, published, 1)){
                    System.out.println("Item: " + videoId + " looks like rendered stream. Send to TG again");
                    sendItemToBot(videoId);
                } else {
                    System.out.println("Item: " + videoId + " not sent to bot due to time between updating and publishing less than 1 day. Looks like a spam");
                }
            } else if (sentItems.get(videoId) == 2) {
                System.out.println("Received item: " + videoId + " send to TG twice already. Not sent to TG now.");
            }

        } else {
            System.out.println("New video item received.");
            BotDb.getInstance().addNewSentItem(newVideo);
            System.out.println("Stored to DB");
            sentItems = BotDb.getInstance().getSentItemsList();
            new SendAfterDelay(this, videoId).start();
            System.out.println("VideoId: " + videoId + " send to postpone delivery worker");
        }
    }

    public void setObserver(YouTubeNotificationsBot observer) {
        this.observer = observer;
    }

    private long stringToDate(String sDate) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(sDate).getTime();
        } catch (ParseException e) {
            System.out.println("stringToDate parser problems");
            e.printStackTrace();
        }
        return timestamp;
    }

    private boolean isSendApproved(String updatedS, String publishedS, int isSendStage) {

        long updated = stringToDate(updatedS);
        long published = stringToDate(publishedS);
        long timeInterval1 = 300000L; //Checking new video, difference between publishing and update should be more than 5 min to approve
        long timeInterval2 = 86400000L; //For checking video which already sent. Diff between publish and update should be more than 1 day to approve

        if (isSendStage == 0 && (updated - published) > timeInterval1) {
            return true;
        } else return (isSendStage == 1 && (updated - published) > timeInterval2);
    }

    public void sendItemToBot(String videoId) {
        String urlToPost = "https://www.youtube.com/watch?v=" + videoId + "&date=" + new Date().getTime();
        System.out.println("New post is send to TG with id: " + videoId);
        BotDb.getInstance().markAsSendById(videoId, (sentItems.get(videoId) + 1));
        sentItems = BotDb.getInstance().getSentItemsList();
        System.out.println("Video marked as send: " + (sentItems.get(videoId) + 1));
        observer.newUpdateReceived(urlToPost);
    }
}


