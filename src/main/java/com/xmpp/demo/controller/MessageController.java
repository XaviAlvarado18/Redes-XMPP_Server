package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.EntityJid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

	       // List<String> messages = new ArrayList<>();
	        try {
	            // Aquí debes implementar la lógica para obtener los mensajes dirigidos al usuario
	            // Esto podría depender de cómo estás almacenando los mensajes (en memoria, en una base de datos, etc.)
	            // Este es solo un ejemplo simple
	        	String username = connection.getUser().asEntityBareJidString();

	        	logger.info("Este es: {}", username);
	            // Obtener los mensajes almacenados para el usuario
	        	List<String> messages = messageService.getMessages(username);
	            response.put("messages", messages);
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