package sample;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

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
        return writer.toString();
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
        return writer.toString();
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
        return writer.toString();
    }

    public static void main(String[] args) throws Exception {
        Integer chatId = 1; // пример с Integer
        String requestXml = getChatReviewsRequest("andrewsis");
        System.out.println(requestXml);
    }
}