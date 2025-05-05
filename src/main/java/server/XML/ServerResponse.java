package server.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class ServerResponse {
    public static String messagesResponse(int chatId) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент ответа
        Element messages = doc.createElement("messages");
        messages.setAttribute("chatId", String.valueOf(chatId));

        String name = "andry";
        String messageTimestamp = "2025-05-05T19:11:00";
        String messageContent = "Hello!";

        Element message = doc.createElement("message");
        Element sender = doc.createElement("sender");
        sender.appendChild(doc.createTextNode(name));
        message.appendChild(sender);
        Element timestamp = doc.createElement("timestamp");
        timestamp.appendChild(doc.createTextNode(messageTimestamp));
        message.appendChild(timestamp);
        Element content = doc.createElement("content");
        content.appendChild(doc.createTextNode(messageContent));
        message.appendChild(content);
        messages.appendChild(message);

        // Добавляем элемент в корень документа
        doc.appendChild(messages);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String statusResponse(int code, String message) throws Exception {
        // Создаем объект DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Создаем новый документ
        Document doc = builder.newDocument();

        // Создаем элемент ответа
        Element status = doc.createElement("status");
        status.setAttribute("code", String.valueOf(code));
        status.appendChild(doc.createTextNode(message));

        // Добавляем элемент в корень документа
        doc.appendChild(status);

        // Преобразуем документ в строку
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String messagesRespondMayBe(List<Chat> chats) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element chatsElem = doc.createElement("chats");

        for (Chat chat : chats) {
            Element chatElem = doc.createElement("chat");

            Element id = doc.createElement("chatId");
            id.appendChild(doc.createTextNode(String.valueOf(chat.chatId)));
            chatElem.appendChild(id);

            Element name = doc.createElement("chatName");
            name.appendChild(doc.createTextNode(chat.chatName));
            chatElem.appendChild(name);

            Element timestamp = doc.createElement("lastMessageTimestamp");
            timestamp.appendChild(doc.createTextNode(chat.lastMessageTimestamp));
            chatElem.appendChild(timestamp);

            Element message = doc.createElement("lastMessage");
            message.appendChild(doc.createTextNode(chat.lastMessage));
            chatElem.appendChild(message);

            chatsElem.appendChild(chatElem);
        }

        doc.appendChild(chatsElem);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static String chatPreviewsRespond(List<Chat> chats) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element chatsElem = doc.createElement("chats");

        for (Chat chat : chats) {
            Element chatElem = doc.createElement("chat");

            Element id = doc.createElement("chatId");
            id.appendChild(doc.createTextNode(String.valueOf(chat.chatId)));
            chatElem.appendChild(id);

            Element name = doc.createElement("chatName");
            name.appendChild(doc.createTextNode(chat.chatName));
            chatElem.appendChild(name);

            Element timestamp = doc.createElement("lastMessageTimestamp");
            timestamp.appendChild(doc.createTextNode(chat.lastMessageTimestamp));
            chatElem.appendChild(timestamp);

            Element message = doc.createElement("lastMessage");
            message.appendChild(doc.createTextNode(chat.lastMessage));
            chatElem.appendChild(message);

            chatsElem.appendChild(chatElem);
        }

        doc.appendChild(chatsElem);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    public static void main(String[] args) throws Exception {
        // Пример ответа на запрос сообщений
        List<Chat> chats = Chat.getChatPreviewByUser("vanya");
        String responseXml = messagesRespondMayBe(chats);
        System.out.println(responseXml);

        // // Пример ответа на успешную отправку сообщения
        // String statusXml = statusResponse(200, "Message delivered");
        // System.out.println(statusXml);

        // // Пример ответа на ошибку
        // String errorXml = statusResponse(400, "Invalid chatId");
        // System.out.println(errorXml);
    }
}