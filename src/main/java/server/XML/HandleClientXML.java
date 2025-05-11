package server.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class HandleClientXML {
    public static String processXml(String xmlString) throws Exception {
        // Парсинг входящего XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));

        // Корневой элемент
        Element root = doc.getDocumentElement();

        String responseXml;

        switch (root.getTagName()) {
            case "getChatReviews" -> {
                String userName = root.getAttribute("userName");
                List<Chat> chats = Chat.getChatPreviewByUser(userName);
                responseXml = ServerResponse.chatPreviewsRespond(chats);
            }
            case "sendMessage" -> {
                NodeList children = root.getChildNodes();
                int chatId = 0;
                String sender = "", content = "";

                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        switch (node.getNodeName()) {
                            case "chatId" -> chatId = Integer.parseInt(node.getTextContent());
                            case "sender" -> sender = node.getTextContent();
                            case "content" -> content = node.getTextContent();
                        }
                    }
                }

                // saveMessage(chatId, sender, content); // Сохраняем сообщение в БД
                responseXml = ServerResponse.statusResponse(200, "Message delivered"); // Успешный ответ
            }
            default -> {
                responseXml = ServerResponse.statusResponse(400, "Unknown request type");
            }
        }

        // Отправка XML-ответа
        return responseXml;
    }
}
