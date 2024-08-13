package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.EntityJid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/xmpp")
public class MessageController {
	
		private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);
		private AbstractXMPPConnection connection;
		
		
		private final MessageService messageService;

	    public MessageController(MessageService messageService) {
	        this.messageService = messageService;
	    }
		
		// Estructura de datos para almacenar los mensajes en memoria
	    private Map<String, List<String>> userMessages = new ConcurrentHashMap<>();
	
		@CrossOrigin(origins = "http://localhost:4200")
		@GetMapping("/get-messages")
		public Map<String, Object> getMessages(HttpSession session) {
			Map<String, Object> response = new HashMap<>();
		
			connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");
		
			if (connection == null) {
				response.put("status", "no connection found in session");
				return response;
			}
		
			try {
				String username = connection.getUser().asEntityBareJidString();
		
				logger.info("Este es: {}", username);
		
				List<MessageXMPP> messages = messageService.getMessages(username);
				response.put("messages", messages);
			} catch (Exception e) {
				logger.error("Failed to get messages", e);
				response.put("status", "error");
				response.put("error", e.getMessage());
			}
		
			return response;
		}

		@CrossOrigin(origins = "http://localhost:4200")
		@GetMapping("/get-messages-user")
		public Map<String, Object> getMessagesBySender(
		        @RequestParam("senderUsername") String senderUsername,
		        HttpSession session) {
		    Map<String, Object> response = new HashMap<>();

		    connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

		    if (connection == null) {
		        response.put("status", "no connection found in session");
		        return response;
		    }

		    try {
		        String username = connection.getUser().asEntityBareJidString();

		        logger.info("Este es: {}", username);

		        // Obtener todos los mensajes
		        List<MessageXMPP> allMessages = messageService.getMessages(username);

		        // Filtrar mensajes recibidos del usuario ingresado
		        List<MessageXMPP> messagesFromSender = allMessages.stream()
		                .filter(message -> message.getSender().equals(senderUsername))
		                .collect(Collectors.toList());

		        // Filtrar mensajes que yo envié al usuario ingresado
		        List<MessageXMPP> myMessages = allMessages.stream()
		                .filter(message -> message.getSender().equals(username) && 
		                                   message.getRecipient().equals(senderUsername))
		                .collect(Collectors.toList());

				logger.info("MyMessages: {}", myMessages);

		        // Combinar ambas listas
		        List<MessageXMPP> combinedMessages = new ArrayList<>();
		        combinedMessages.addAll(messagesFromSender);
		        combinedMessages.addAll(myMessages);

		        response.put("messages", combinedMessages);
		    } catch (Exception e) {
		        logger.error("Failed to get messages", e);
		        response.put("status", "error");
		        response.put("error", e.getMessage());
		    }

		    return response;
		}	
	 	
	 	
	 	@CrossOrigin(origins = "http://localhost:4200")
	    @PostMapping("/send-message")
	    public Map<String, String> sendMessage(@RequestParam("to") String to, @RequestParam("body") String body, HttpSession session) {
	        Map<String, String> response = new HashMap<>();

	        connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

	        if (connection == null) {
	            response.put("status", "no connection found in session");
	            return response;
	        }

	        try {
	            messageService.sendMessage(connection, to, body);
	            response.put("status", "message sent");
	        } catch (Exception e) {
	            logger.error("Failed to send message", e);
	            response.put("status", "error");
	            response.put("error", e.getMessage());
	        }

	        return response;
	    }
	
}