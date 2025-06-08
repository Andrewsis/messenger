package server.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import common.ChatPreviewServ;
import common.GroupInfoServ;
import common.MessageServ;

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
            case "login" -> {
                responseXml = ServerResponse.statusResponse(200, "Login successful");
            }
            case "getChatReviews" -> {
                String userName = root.getAttribute("userName");
                List<ChatPreviewServ> chats = ChatPreviewServ.getChatPreview(userName);
                responseXml = ServerResponse.getChatReviewsResponse(chats);
            }
            case "getAllUsers" -> {
                responseXml = ServerResponse.getAllUsersResponse();
            }
            case "getMessages" -> {
                int chatId = Integer.parseInt(root.getAttribute("chatId"));
                List<MessageServ> messages = MessageServ.getMessages(chatId);
                responseXml = ServerResponse.getMessagesResponse(messages);
            }
            case "createChat" -> {
                String ourUsername = root.getAttribute("ourUsername");
                String otherUsername = root.getAttribute("otherUsername");
                String chatName = root.getAttribute("chatName");
                String chatDesc = root.getAttribute("chatDesc");
                responseXml = ServerResponse.createChatResponse(ourUsername, otherUsername, chatName, chatDesc);
            }
            case "sendMessage" -> {
                int chatId = Integer.parseInt(root.getAttribute("chatId"));
                String sender = root.getAttribute("sender");
                String content = root.getAttribute("content");
                List<MessageServ> messages = MessageServ.sendMessage(chatId, content, sender);

                responseXml = ServerResponse.getMessagesResponse(messages);
            }
            case "getGroupInfo" -> {
                int chatId = Integer.parseInt(root.getAttribute("chatId"));
                GroupInfoServ groupInfo = GroupInfoServ.getGroupInfo(chatId);
                responseXml = ServerResponse.getGroupInfoResponse(groupInfo);
            }
            case "removeUser" -> {
                String username = root.getAttribute("username");
                int chatId = Integer.parseInt(root.getAttribute("chatId"));

                responseXml = ServerResponse.removeUserFromChatResponse(username, chatId);
            }
            case "addUser" -> {
                String username = root.getAttribute("username");
                int chatId = Integer.parseInt(root.getAttribute("chatId"));

                responseXml = ServerResponse.addUserToChatResponse(username, chatId);
            }
            default -> {
                responseXml = ServerResponse.statusResponse(400, "Unknown request type");
            }
        }

        // Отправка XML-ответа
        return responseXml + "<END_OF_MESSAGE>";
    }
}
