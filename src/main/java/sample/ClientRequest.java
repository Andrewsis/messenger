package sample;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import common.ChatPreview;
import common.GroupInfo;
import common.Message;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class ClientRequest {
    public static String getMessagesRequest(Integer chatId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element getMessages = doc.createElement("getMessages");
        getMessages.setAttribute("chatId", chatId.toString());

        doc.appendChild(getMessages);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String sendMessageRequest(Integer chatId, String sender, String content) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element sendMessage = doc.createElement("sendMessage");

        sendMessage.setAttribute("chatId", chatId.toString());
        sendMessage.setAttribute("sender", sender);
        sendMessage.setAttribute("content", content);

        doc.appendChild(sendMessage);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String getChatReviewsRequest(String userName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element getChatReviews = doc.createElement("getChatReviews");
        getChatReviews.setAttribute("userName", userName);

        doc.appendChild(getChatReviews);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String getAllUsersRequest() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element getAllUsers = doc.createElement("getAllUsers");

        doc.appendChild(getAllUsers);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    // Members in group and group bio
    public static String getGroupInfoRequest(int chatId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element getGroupInfo = doc.createElement("getGroupInfo");
        getGroupInfo.setAttribute("chatId", String.valueOf(chatId));

        doc.appendChild(getGroupInfo);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String removeUserFromChatRequest(String username, int chatId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element removeUser = doc.createElement("removeUser");
        removeUser.setAttribute("username", username);
        removeUser.setAttribute("chatId", String.valueOf(chatId));

        doc.appendChild(removeUser);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String addUserToChatRequest(String user, int chatId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element addUser = doc.createElement("addUser");
        addUser.setAttribute("username", user);
        addUser.setAttribute("chatId", String.valueOf(chatId));

        doc.appendChild(addUser);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String sendLoginRequest(String userName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element login = doc.createElement("login");
        login.setAttribute("userName", userName);

        doc.appendChild(login);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String createChatRequest(String ourUsername, String otherUsername, String chatName, String chatDesc)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element createChat = doc.createElement("createChat");
        createChat.setAttribute("ourUsername", ourUsername);
        createChat.setAttribute("otherUsername", otherUsername);
        createChat.setAttribute("chatName", chatName);
        createChat.setAttribute("chatDesc", chatDesc);

        doc.appendChild(createChat);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }

    public static String sendLoginRequest(String userName, String password) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        Element login = doc.createElement("login");
        login.setAttribute("userName", userName);
        login.setAttribute("password", password);

        doc.appendChild(login);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString() + "<END_OF_MESSAGE>";
    }
    // PARSING XML

    public static List<ChatPreview> parseChatPreviews(String responseXml) {
        List<ChatPreview> chatPreviews = new ArrayList<>();

        if (responseXml == null || responseXml.trim().isEmpty()) {
            System.err.println("responseXml is empty or null. Cannot parse.");
            return chatPreviews;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

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
                        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                .appendPattern("yyyy-MM-dd HH:mm:ss")
                                .optionalStart()
                                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                                .optionalEnd()
                                .toFormatter();
                        lastTimestamp = LocalDateTime.parse(timestampStr, formatter);
                    } catch (Exception e) {
                        System.err.println("Invalid timestamp format: " + e.getMessage());
                    }

                    int membersQuantity = 0;
                    try {
                        membersQuantity = Integer.parseInt(getTagContent(chatElement, "membersQuantity"));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid membersQuantity format: " + e.getMessage());
                    }

                    ChatPreview preview = new ChatPreview(chatId, chatName, lastMessage, lastTimestamp,
                            membersQuantity);

                    chatPreviews.add(preview);
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing chat previews: " + e.getMessage());
            e.printStackTrace();
        }

        return chatPreviews;
    }

    public static List<Message> parseChatMessages(String responseXml) {
        List<Message> messages = new ArrayList<>();

        if (responseXml == null || responseXml.trim().isEmpty()) {
            System.err.println("responseXml is empty or null. Cannot parse.");
            return messages;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
            doc.getDocumentElement().normalize();

            NodeList chatList = doc.getElementsByTagName("message");

            for (int i = 0; i < chatList.getLength(); i++) {
                Node chatNode = chatList.item(i);

                if (chatNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element chatElement = (Element) chatNode;

                    String content = getTagContent(chatElement, "content");
                    String username = getTagContent(chatElement, "username");
                    String timestampStr = getTagContent(chatElement, "lastMessageTimestamp");

                    LocalDateTime lastTimestamp = null;
                    try {
                        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                .appendPattern("yyyy-MM-dd HH:mm:ss")
                                .optionalStart()
                                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                                .optionalEnd()
                                .toFormatter();
                        lastTimestamp = LocalDateTime.parse(timestampStr, formatter);
                    } catch (Exception e) {
                        System.err.println("Invalid timestamp format: " + e.getMessage());
                    }

                    Message message = new Message(content, username, lastTimestamp);
                    messages.add(message);
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing chat previews: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public static List<String> parseAllUsers(String responseXml) {
        List<String> users = new ArrayList<>();

        if (responseXml == null || responseXml.trim().isEmpty()) {
            System.err.println("responseXml is empty or null. Cannot parse.");
            return users;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            responseXml = responseXml.replace("<END_OF_MESSAGE>", "").trim();

            Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
            doc.getDocumentElement().normalize();

            NodeList usernameList = doc.getElementsByTagName("username");

            for (int i = 0; i < usernameList.getLength(); i++) {
                Node usernameNode = usernameList.item(i);
                if (usernameNode.getNodeType() == Node.ELEMENT_NODE) {
                    String username = usernameNode.getTextContent();
                    if (username != null && !username.trim().isEmpty()) {
                        users.add(username.trim());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public static GroupInfo parseGroupInfo(String responseXml) {
        if (responseXml == null || responseXml.trim().isEmpty()) {
            System.err.println("responseXml is empty or null. Cannot parse.");
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            responseXml = responseXml.replace("<END_OF_MESSAGE>", "").trim();

            Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
            doc.getDocumentElement().normalize();

            NodeList groupList = doc.getElementsByTagName("groupInfo");

            if (groupList.getLength() > 0) {
                Node groupNode = groupList.item(0);

                if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element groupElement = (Element) groupNode;

                    NodeList membersNodes = groupElement.getElementsByTagName("member");
                    List<String> members = new ArrayList<>();
                    for (int j = 0; j < membersNodes.getLength(); j++) {
                        members.add(membersNodes.item(j).getTextContent());
                    }

                    String groupBio = getTagContent(groupElement, "groupBio");

                    return new GroupInfo(members, groupBio);
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing group info: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // method to extract SECURELY content from a specific tag
    private static String getTagContent(Element element, String tagName) {
        Node node = element.getElementsByTagName(tagName).item(0);
        return node != null ? node.getTextContent() : "";
    }

    public static void main(String[] args) throws Exception {
        Integer chatId = 1;
        String requestXml = getMessagesRequest(chatId);
        System.out.println(requestXml);
    }
}