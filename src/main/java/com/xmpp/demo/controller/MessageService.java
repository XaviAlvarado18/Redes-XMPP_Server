package com.xmpp.demo.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
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

                    // Obtener la fecha y hora actual en el formato "dd/MM HH:mm"
                    String dateTimeMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

                    // Crear un nuevo objeto MessageXMPP con la información adicional
                    MessageXMPP msg = new MessageXMPP(message.getBody(), from.asEntityBareJidString(), dateTimeMsg, to);

                    // Almacenar el mensaje en la lista correspondiente al usuario
                    userMessages.computeIfAbsent(to, k -> new ArrayList<>()).add(msg);

                    // Almacenar el mensaje en la lista correspondiente al usuario remitente
                    userMessages.computeIfAbsent(from.asEntityBareJidString(), k -> new ArrayList<>()).add(msg);
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
        try {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(to));
            
            // Enviar mensaje
            chat.send(body);

            // Obtener la fecha y hora actual en el formato "dd/MM HH:mm"
            String dateTimeMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

            // Crear un nuevo objeto MessageXMPP con la información adicional
            MessageXMPP sentMessage = new MessageXMPP(body, connection.getUser().asEntityBareJidString(), dateTimeMsg, to);
            
            // Almacenar el mensaje para ambos, remitente y destinatario
            userMessages.computeIfAbsent(to, k -> new ArrayList<>()).add(sentMessage);
            userMessages.computeIfAbsent(connection.getUser().asEntityBareJidString(), k -> new ArrayList<>()).add(sentMessage);
            
            logger.info("Message sent from {} to {}: {}", connection.getUser().asEntityBareJidString(), to, body);
        } catch (Exception e) {
            logger.error("Failed to send message", e);
        }
    }

    public void sendGroupMessage(XMPPConnection connection, String groupName, String body) throws XmppStringprepException, IOException, InterruptedException, XMPPException, SmackException {
        try {
            String groupJid = groupName + "@conference.alumchat.lol";
            EntityBareJid groupEntityJid = JidCreate.entityBareFrom(groupJid);
            
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupEntityJid);

            // Si no estás unido al grupo, únete primero
            if (!multiUserChat.isJoined()) {
            	MucEnterConfiguration.Builder builder = multiUserChat.getEnterConfigurationBuilder(
            		    Resourcepart.from(connection.getUser().getLocalpart().toString())
            	);
                MucEnterConfiguration mucEnterConfiguration = builder.build();
                multiUserChat.join(mucEnterConfiguration);
            }

            // Enviar mensaje
            multiUserChat.sendMessage(body);

            // Obtener la fecha y hora actual en el formato "dd/MM HH:mm"
            String dateTimeMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

            // Crear un nuevo objeto MessageXMPP con la información adicional
            MessageXMPP sentMessage = new MessageXMPP(body, connection.getUser().asEntityBareJidString(), dateTimeMsg, groupJid);

            // Almacenar el mensaje en tu estructura, si es necesario

            logger.info("Message sent to group {}: {}", groupJid, body);
        } catch (Exception e) {
            logger.error("Failed to send group message", e);
        }
    }
    
}
