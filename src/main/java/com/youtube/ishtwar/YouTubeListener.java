package com.youtube.ishtwar;

/*To implement
 * 1. HttpServer based on NanoHttpD
 * 2. Test Request parser
 * 3. Check/Restore subscribtion status*/

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class YouTubeListener extends NanoHTTPD {
    private YouTubeNotificationsBot observer;

    public YouTubeListener(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\b NanoHTTPD Running!");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            try {
                Map<String, String> body = new HashMap<>();
                session.parseBody(body);
                for (Map.Entry entry : body.entrySet()) {
                    xmlParser(entry.getValue().toString());
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

    private void xmlParser(String xml) { //Магия парсинга, в 2 захода парсим ATOM feed от ютуба
        NodeList nl = null;
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            //вытаскиваем грязный урл вида href="http://www.youtube.com/watch?v=VIDEO_ID"
            XPathExpression expr = xpath.compile("//feed/entry/link/@href");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            // Output NodeList
            for (int i = 0; i < nl.getLength(); i++) {
                //приводим урл к конечному виду и записываем в переменную http://www.youtube.com/watch?v=VIDEO_ID
                notifyObservers(nl.item(i).toString().substring(6, nl.item(i).toString().length() - 1));
            }
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setObserver(YouTubeNotificationsBot observer) {
        this.observer = observer;
    }

    private void notifyObservers(String urlToPost){
        observer.newUpdateReceived(urlToPost);
    }
}
