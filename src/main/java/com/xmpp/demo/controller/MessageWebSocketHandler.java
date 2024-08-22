package com.xmpp.demo.controller;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    static final List<MessageXMPP> messageQueue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Aquí envías el mensaje recibido desde el backend al frontend
        session.sendMessage(new TextMessage("Mensaje recibido: " + message.getPayload()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        // Opcional: Enviar todos los mensajes acumulados al nuevo cliente
        synchronized (messageQueue) {
            for (MessageXMPP msg : messageQueue) {
                String jsonMessage = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public static void addMessage(MessageXMPP groupMessage) {
        synchronized (messageQueue) {
            messageQueue.add(groupMessage);
        }
        // Enviar mensaje a través de WebSocket a los clientes conectados
        sendMessageToAll(groupMessage);
    }

    public static void sendMessageToAll(MessageXMPP groupMessage) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(groupMessage);
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
