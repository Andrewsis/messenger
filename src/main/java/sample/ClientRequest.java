package sample;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientRequest {
    public static String getMessagesRequest(Integer chatId) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент запроса
        Element getMessages = doc.createElement("getMessages");
        getMessages.setAttribute("chatId", chatId.toString()); // Преобразуем Integer в String

        // Добавляем элемент в корень документа
        doc.appendChild(getMessages);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String sendMessageRequest(Integer chatId, String sender, String content) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент запроса
        Element sendMessage = doc.createElement("sendMessage");

        // Добавляем вложенные элементы
        Element chatIdElement = doc.createElement("chatId");
        chatIdElement.appendChild(doc.createTextNode(chatId.toString())); // Преобразуем Integer в String
        sendMessage.appendChild(chatIdElement);

        Element senderElement = doc.createElement("sender");
        senderElement.appendChild(doc.createTextNode(sender));
        sendMessage.appendChild(senderElement);

        Element contentElement = doc.createElement("content");
        contentElement.appendChild(doc.createTextNode(content));
        sendMessage.appendChild(contentElement);

        // Добавляем элемент в корень документа
        doc.appendChild(sendMessage);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String getChatReviewsRequest(String userName) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент запроса
        Element getChatReviews = doc.createElement("getChatReviews");
        getChatReviews.setAttribute("userName", userName);

        // Добавляем элемент в корень документа
        doc.appendChild(getChatReviews);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static List<ChatPreview> parseChatPreviews(String responseXml) {
        List<ChatPreview> chatPreviews = new ArrayList<>();

        System.out.println("Parsing XML: " + responseXml);
        if (responseXml == null || responseXml.trim().isEmpty()) {
            System.err.println("responseXml is empty or null. Cannot parse.");
            return chatPreviews;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Удаляем возможный маркер <END_OF_MESSAGE>
            responseXml = responseXml.replace("<END_OF_MESSAGE>", "").trim();

            Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
            doc.getDocumentElement().normalize();

            NodeList chatList = doc.getElementsByTagName("chat");

            for (int i = 0; i < chatList.getLength(); i++) {
                Node chatNode = chatList.item(i);

                if (chatNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element chatElement = (Element) chatNode;

                    int chatId = 0;
                    try {
                        chatId = Integer.parseInt(getTagContent(chatElement, "chatId"));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid chatId format: " + e.getMessage());
                    }
                    String chatName = getTagContent(chatElement, "chatName");
                    String lastMessage = getTagContent(chatElement, "lastMessage");
                    String timestampStr = getTagContent(chatElement, "lastMessageTimestamp");

                    LocalDateTime lastTimestamp = null;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");
                        lastTimestamp = LocalDateTime.parse(timestampStr, formatter);
                    } catch (Exception e) {
                        System.err.println("Invalid timestamp format: " + e.getMessage());
                    }

                    ChatPreview preview = new ChatPreview(chatId, chatName, lastMessage, lastTimestamp);
                    chatPreviews.add(preview);
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing chat previews: " + e.getMessage());
            e.printStackTrace();
        }

        return chatPreviews;
    }

    // Вспомогательный метод для безопасного получения значения тега
    private static String getTagContent(Element element, String tagName) {
        Node node = element.getElementsByTagName(tagName).item(0);
        return node != null ? node.getTextContent() : "";
    }

    public static void main(String[] args) throws Exception {
        Integer chatId = 1; // пример с Integer
        String requestXml = getMessagesRequest(chatId);
        System.out.println(requestXml);
    }
}