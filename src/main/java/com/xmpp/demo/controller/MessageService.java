package com.xmpp.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;


import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager;
import org.jivesoftware.smackx.httpfileupload.UploadService;
import org.jivesoftware.smackx.httpfileupload.element.Slot;
import org.jivesoftware.smackx.httpfileupload.element.SlotRequest;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
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
    
    public List<MessageXMPP> getMessagesGroup(String jid) {
        logger.info("Retrieving messages for: {}", jid);
        List<MessageXMPP> messages = userMessages.getOrDefault(jid, new ArrayList<>());

        // Log para verificar los mensajes recuperados
        if (messages.isEmpty()) {
            logger.warn("No messages found for: {}", jid);
        } else {
            logger.info("Messages for {}: {}", jid, messages);
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
            // Construir el JID del grupo
            String groupJid = groupName + "@conference.alumchat.lol";
            logger.info("Group JID: {}", groupJid);

            // Crear el EntityBareJid para el grupo
            EntityBareJid groupEntityJid = JidCreate.entityBareFrom(groupJid);
            logger.info("Group EntityBareJid: {}", groupEntityJid);

            // Obtener el MultiUserChatManager y MultiUserChat para el grupo
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupEntityJid);
            logger.info("MultiUserChat created: {}", multiUserChat);

            // Verificar si el usuario está unido al grupo, si no, unirse
            if (!multiUserChat.isJoined()) {
                logger.info("User not joined to the group, attempting to join...");
                MucEnterConfiguration.Builder builder = multiUserChat.getEnterConfigurationBuilder(
                    Resourcepart.from(connection.getUser().getLocalpart().toString())
                );
                MucEnterConfiguration mucEnterConfiguration = builder.build();
                multiUserChat.join(mucEnterConfiguration);
                logger.info("Joined the group: {}", groupJid);
            } else {
                logger.info("Already joined to the group: {}", groupJid);
            }

            // Enviar el mensaje al grupo
            logger.info("Sending message to group {}: {}", groupJid, body);
            multiUserChat.sendMessage(body);
            logger.info("Message successfully sent to group: {}", groupJid);

            // Obtener la fecha y hora actual en el formato "dd/MM HH:mm"
            String dateTimeMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            logger.info("Message timestamp: {}", dateTimeMsg);

            // Crear un nuevo objeto MessageXMPP con la información adicional
            MessageXMPP sentMessage = new MessageXMPP(body, connection.getUser().asEntityBareJidString(), dateTimeMsg, groupJid);
            logger.info("Created MessageXMPP object: {}", sentMessage);

            // Almacenar el mensaje en tu estructura, si es necesario
            // Aquí podrías añadir lógica para almacenar o procesar el mensaje si lo necesitas

        } catch (Exception e) {
            logger.error("Failed to send group message", e);
            throw e;  // Lanzar la excepción para poder manejarla adecuadamente en otros lugares
        }
    }
    
    
    public void sendFile(AbstractXMPPConnection connection, String to, String base64File, String fileName) throws Exception {
    	// Verificar que el archivo sea un archivo de texto
        if (!fileName.endsWith(".txt")) {
            throw new Exception("Solo se permiten archivos .txt");
        }

        // Decodificar el archivo de Base64
        byte[] fileBytes = Base64.getDecoder().decode(base64File);

        // Convertir los bytes a texto
        String fileContent = new String(fileBytes);

        // Crear un JID para el destinatario
        EntityBareJid bareJid = JidCreate.entityBareFrom(to);
        
        // Obtener el Roster y asegurarse de que esté cargado
        Roster roster = Roster.getInstanceFor(connection);
        roster.reloadAndWait();

        logger.info("Enviando contenido del archivo a: {}", bareJid);
        logger.info("Tamaño del archivo: {} bytes", fileBytes.length);
        logger.info("Nombre del archivo: {}", fileName);
        
        // Obtener la presencia del contacto
        Presence presence = roster.getPresence(bareJid);
        if (!presence.isAvailable()) {
            throw new Exception("El usuario " + to + " no está disponible para recibir archivos.");
        }

        // Enviar el contenido del archivo como un mensaje de chat
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.chatWith(bareJid).send("Contenido del archivo '" + fileName + "':\n" + fileContent);

        logger.info("Contenido del archivo enviado como mensaje de texto.");
    }
    
    
 // Agregar este método a tu clase
    private static void disableSSLVerification() {
        try {
            // Crear un trust manager que no valide las cadenas de certificados
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Instalar el trust manager all-trusting
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Crear un host name verifier all-trusting
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Instalar el host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public void sendNewFile(AbstractXMPPConnection connection, String to, String base64File, String fileName) throws Exception {
    	
    	disableSSLVerification();
    	
        // Decodificar el archivo de Base64
        byte[] fileBytes = Base64.getDecoder().decode(base64File);

        // Crear un JID para el destinatario
        EntityBareJid bareJid = JidCreate.entityBareFrom(to);

        // Obtener el HttpFileUploadManager
        HttpFileUploadManager uploadManager = HttpFileUploadManager.getInstanceFor(connection);

        if (!uploadManager.isUploadServiceDiscovered()) {
            throw new Exception("El servidor no soporta HTTP File Upload.");
        }

        // Crear una solicitud de slot
        //SlotRequest slotRequest = new SlotRequest((DomainBareJid) connection.getUser().asBareJid(), fileName, fileBytes.length, "image/png");

        // Solicitar un slot
        Slot slot = uploadManager.requestSlot(fileName, fileBytes.length, "image/png");

        if (slot == null) {
            throw new Exception("No se pudo obtener un slot para la carga del archivo.");
        }

        // Subir el archivo
        URL putUrl = slot.getPutUrl();
        HttpURLConnection httpCon = (HttpURLConnection) putUrl.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        try (OutputStream os = httpCon.getOutputStream()) {
            os.write(fileBytes);
        }

        int responseCode = httpCon.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            throw new Exception("Error al subir el archivo. Código de respuesta: " + responseCode);
        }

        // Obtener la URL de descarga
        String getUrl = slot.getGetUrl().toString();

        // Enviar un mensaje con la URL del archivo
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.chatWith(bareJid).send("Te he enviado un archivo: " + getUrl);
    }

}
