package com.xmpp.demo.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
	

    private Map<String, List<MessageXMPP>> userMessages = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);

    public void initialize(AbstractXMPPConnection connection) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        logger.info("ChatManager initialized for connection: {}", connection);

        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, org.jivesoftware.smack.packet.Message message, org.jivesoftware.smack.chat2.Chat chat) {
                try {
                    String to = ((EntityJid) message.getTo()).asEntityBareJidString();
                    logger.info("Received message from {}: {}. To: {}", from, message.getBody(), to);

                    // Obtener la fecha actual en el formato "dd/MM"
                    String date_msg = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"));

                    // Crear un nuevo objeto Message con la información adicional
                    MessageXMPP msg = new MessageXMPP(message.getBody(), from.asEntityBareJidString(), date_msg);

                    // Almacenar el mensaje en la lista correspondiente al usuario
                    userMessages.computeIfAbsent(to, k -> new ArrayList<>()).add(msg);
                } catch (Exception e) {
                    logger.error("Error processing incoming message", e);
                }
            }
        });

        logger.info("Incoming message listener added to ChatManager");
    }

    public List<MessageXMPP> getMessages(String username) {
        logger.info("Retrieving messages for user: {}", username);
        List<MessageXMPP> messages = userMessages.getOrDefault(username, new ArrayList<>());

        // Log para verificar los mensajes recuperados
        if (messages.isEmpty()) {
            logger.warn("No messages found for user: {}", username);
        } else {
            logger.info("Messages for user {}: {}", username, messages);
        }

        return messages;
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
