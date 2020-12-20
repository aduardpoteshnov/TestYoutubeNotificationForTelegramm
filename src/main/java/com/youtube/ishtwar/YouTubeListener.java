package com.youtube.ishtwar;

import com.youtube.ishtwar.db.BotDb;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.StringReader;

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
    private List<String> alreadySentItems;

    public YouTubeListener(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        alreadySentItems = BotDb.getInstance().getSentItemsList();
        for(int i = 0; i < alreadySentItems.size(); i++){
            System.out.println(alreadySentItems.get(i));
        }
        System.out.println("\b NanoHTTPD Running!");
    }

    @Override
    public Response serve(IHTTPSession session) {  //Ловим хттп реквест
        if (session.getMethod() == Method.POST) {   //post ожидаем только от ютубчика
            try {
                Map<String, String> body = new HashMap<>();
                session.parseBody(body); //вытаскиваем бодик
                for (Map.Entry entry : body.entrySet()) {
                    handleNewlyReceivedVideo(xmlParser(entry.getValue().toString())); //отправляем xml полученый из бодика и парсим его в еще одну хешмапу и прям бегом обрабатываем полученную мапу
                }
                return newFixedLengthResponse("OK");
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
        }

        if (session.getMethod() == Method.GET) {
            return newFixedLengthResponse(session.getParameters().get("hub.challenge").get(0));
        }
        return newFixedLengthResponse(Response.Status.ACCEPTED, MIME_PLAINTEXT,
                "ACCEPTED");
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
                youtubeRequest.put("videoId",  getCharacterDataFromElementXmlParserPart(line));

                NodeList published = element.getElementsByTagName("published");
                line = (Element) published.item(0);
                youtubeRequest.put("published", getCharacterDataFromElementXmlParserPart(line));

                NodeList updated = element.getElementsByTagName("updated");
                line = (Element) updated.item(0);
                youtubeRequest.put("updated",  getCharacterDataFromElementXmlParserPart(line));
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

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    private void handleNewlyReceivedVideo(HashMap<String, String> newVideo){
        String videoId = newVideo.get("videoId");
        String date = newVideo.get("updated");
        if(alreadySentItems.contains(videoId)){
            HashMap<String, String > oldItem = BotDb.getInstance().getSentItemByVid(videoId);
            String oldDate = oldItem.get("updated");
            if(isTimeBetweenUpdatesIsOK(date, oldDate)){
                BotDb.getInstance().updateSentItems(newVideo);
                observer.newUpdateReceived("https://www.youtube.com/watch?v=" + videoId + "&date=" + date);
            }else {
                observer.newUpdateReceived("Service Message, \n " +
                        "Interval between updates the same video is too short. \n" +
                        "Interval value is 60min now");
            }
        }else {
            BotDb.getInstance().addNewSentItem(newVideo);
            alreadySentItems = BotDb.getInstance().getSentItemsList();
            observer.newUpdateReceived("https://www.youtube.com/watch?v=" + videoId + "&date=" + date);
        }
    }

    public void setObserver(YouTubeNotificationsBot observer) {
        this.observer = observer;
    }

    private boolean isTimeBetweenUpdatesIsOK(String newDate, String oldDate){
        System.out.println(newDate);
        System.out.println(oldDate);
        long okTime = convertMinToMs(60);
        return (parseDate(newDate) - parseDate(oldDate) >= okTime);
    }

    private Long parseDate(String dateToParse){
        long dateToReturn = 578924129106L;
        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            dateToReturn = sdf.parse(dateToParse).getTime();
            System.out.println("parsedDate " + dateToReturn);
        } catch (ParseException e) {
            System.out.println("ParseDate error");
            e.printStackTrace();
        }
        return dateToReturn;
    }

    private Long convertMinToMs(int min){
        return min * 60000L;
    }
}
