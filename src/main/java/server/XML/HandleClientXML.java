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
import server.ClientHandler;
import server.Server;

public class HandleClientXML {
    public static String processXml(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();

        String responseXml;

        switch (root.getTagName()) {
            case "login" -> {
                String userName = root.getAttribute("userName");
                String password = root.getAttribute("password");
                responseXml = ServerResponse.loginResponse(userName, password);
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

                for (ClientHandler client : Server.getClients()) {
                    if (client.getUserName().equals(otherUsername)) {
                        List<ChatPreviewServ> chats = ChatPreviewServ.getChatPreview(otherUsername);
                        String chatPreviewsXml = ServerResponse.getChatReviewsResponse(chats);
                        client.sendMessage(chatPreviewsXml + "<END_OF_MESSAGE>");
                        break;
                    }
                }
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
                for (ClientHandler client : Server.getClients()) {
                    if (client.getUserName().equals(username)) {
                        String chatPreviewsXml = ServerResponse.getChatReviewsResponse(
                                ChatPreviewServ.getChatPreview(username));
                        client.sendMessage(chatPreviewsXml + "<END_OF_MESSAGE>");
                        break;
                    }
                }
            }
            case "addUser" -> {
                String username = root.getAttribute("username");
                int chatId = Integer.parseInt(root.getAttribute("chatId"));

                responseXml = ServerResponse.addUserToChatResponse(username, chatId);

                for (ClientHandler client : Server.getClients()) {
                    if (client.getUserName().equals(username)) {
                        List<ChatPreviewServ> chats = ChatPreviewServ.getChatPreview(username);
                        String chatPreviewsXml = ServerResponse.getChatReviewsResponse(chats);
                        client.sendMessage(chatPreviewsXml + "<END_OF_MESSAGE>");
                        break;
                    }
                }
            }
            default -> {
                responseXml = ServerResponse.statusResponse(400, "Unknown request type");
            }
        }

        return responseXml + "<END_OF_MESSAGE>";
    }
}
