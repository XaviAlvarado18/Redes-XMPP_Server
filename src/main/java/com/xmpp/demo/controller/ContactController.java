package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.xmpp.demo.service.ContactService;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xmpp")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private AbstractXMPPConnection connection;

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/get-contacts")
    public Map<String, Object> getContacts(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

        if (connection == null) {
            response.put("status", "no connection found in session");
            return response;
        }

        try {
            String username = connection.getUser().asEntityBareJidString();

            logger.info("Getting contacts for: {}", username);

            List<ContactXMPP> contacts = contactService.getContacts(username, connection);
            response.put("contacts", contacts);
        } catch (Exception e) {
            logger.error("Failed to get contacts", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }

        return response;
    }

}
