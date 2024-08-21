package com.xmpp.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/xmpp")
public class ConferenceController {

    private AbstractXMPPConnection connection;
		
	private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);	
	private final ConferenceService conferenceService;

	public ConferenceController(ConferenceService conferenceService) {
	        this.conferenceService = conferenceService;
	}

    @CrossOrigin(origins = "http://localhost:4200")
		@PostMapping("/create-group")
		public Map<String, String> createGroup(@RequestBody GroupRequest groupRequest, HttpSession session) {
			Map<String, String> response = new HashMap<>();
			connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

			if (connection == null) {
				response.put("status", "no connection found in session");
				return response;
			}

			try {
				// Llama al método para crear la conferencia
				conferenceService.createConference(connection, groupRequest);

				// Construye el JID del grupo
				String groupName = groupRequest.getgroupName();
				String conferenceJid = groupName + "@conference.alumchat.lol";

				// Construye la respuesta
				response.put("status", "group created");
				response.put("groupId", conferenceJid); // Retorna el JID del grupo creado
			} catch (Exception e) {
				logger.error("Failed to create group", e);
				response.put("status", "error");
				response.put("error", e.getMessage());
			}

			return response;
		}


		@CrossOrigin(origins = "http://localhost:4200")
		@GetMapping("/get-groups")
		public List<String> getGroups(HttpSession session, @RequestParam("username") String username) {
			connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

			if (connection == null) {
				return Collections.singletonList("No connection found in session");
			}

			try {
				return conferenceService.getJoinedGroups(connection, username);
			} catch (Exception e) {
				// Manejar la excepción y devolver un mensaje de error
				e.printStackTrace();
				return Collections.singletonList("Failed to retrieve groups: " + e.getMessage());
			}
		}

}
