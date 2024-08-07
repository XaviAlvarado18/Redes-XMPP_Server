package com.xmpp.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
	
    private Map<String, List<String>> userMessages = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);
    
    public void initialize(AbstractXMPPConnection connection) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        logger.info("ChatManager initialized for connection: {}", connection);
        
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {
                try {
                    String to = ((EntityJid) message.getTo()).asEntityBareJidString();
                    logger.info("Received message from {}: {}. To: {}", from, message.getBody(), to);
                    
                    userMessages.computeIfAbsent(to, k -> new ArrayList<>()).add(message.getBody());
                } catch (Exception e) {
                    logger.error("Error processing incoming message", e);
                }
            }
        });

        logger.info("Incoming message listener added to ChatManager");
    }
    
    public List<String> getMessages(String username) {
        logger.info("Retrieving messages for user: {}", username);
        List<String> messages = userMessages.getOrDefault(username, new ArrayList<>());

        // Log para verificar los mensajes recuperados
        if (messages.isEmpty()) {
            logger.warn("No messages found for user: {}", username);
        } else {
            logger.info("Messages for user {}: {}", username, messages);
        }
        
        return messages;
    }


    public void addMessage(String username, String message) {
        userMessages.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
    }

    public void initializeUserMessages(String username) {
        userMessages.putIfAbsent(username, new ArrayList<>());
    }
    
    public void sendMessage(AbstractXMPPConnection connection, String to, String body) throws XmppStringprepException, IOException, InterruptedException, XMPPException, NotConnectedException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(to);
        Chat chat = chatManager.chatWith(jid);

        Message message = new Message(jid, Message.Type.chat);
        message.setBody(body);

        chat.send(message);
        logger.info("Sent message to {}: {}", to, body);
    }
    
}
