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
	    public Map<String, Object> getGroups(HttpSession session) {
	        Map<String, Object> response = new HashMap<>();
	        try {
	        	connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");
	        	
	            // Obtener el nombre de usuario desde la sesión
	        	String username = connection.getUser().asEntityBareJidString();
	        	logger.info("Este es: {}", username);
	        	
	            // Obtener los grupos donde el usuario está presente
	            List<GroupRequest> groups = conferenceService.getGroupsForUser(username);
	
	            response.put("status", "success");
	            response.put("groups", groups);
	        } catch (Exception e) {
	            response.put("status", "error");
	            response.put("error", e.getMessage());
	        }
	
	        return response;
	    }


}
