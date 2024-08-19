package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//import com.xmpp.demo.service.ContactService;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/xmpp")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private AbstractXMPPConnection connection;

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Value("${xmpp.domain}")
    private String domain;
    
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
    
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/add")
    public ResponseEntity<String> addContact(HttpSession session, @RequestParam("username") String username) throws XmppStringprepException {
        Logger logger = LoggerFactory.getLogger(ContactController.class);

        connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");
        
        if (connection == null) {
            logger.error("No active session found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session found.");
        }

        try {
            logger.info("Obtaining roster for user: {}", username);
            Roster roster = Roster.getInstanceFor(connection);

            Jid jid = JidCreate.from(username + "@" + domain); // Reemplaza con el dominio de tu servidor
            logger.info("Constructed JID: {}", jid);

            Set<RosterEntry> entries = roster.getEntries();
            logger.info("Existing contacts count: {}", entries.size());

            boolean userExists = false;
            for (RosterEntry entry : entries) {
                logger.info("Checking entry: {}", entry.getJid());
                if (entry.getJid().equals(jid)) {
                    userExists = true;
                    logger.info("User {} already in contacts.", username);
                    break;
                }
            }

            if (userExists) {
                logger.info("User already in contacts.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already in contacts.");
            }

            logger.info("Adding user {} to contacts.", username);
            roster.createEntry((BareJid) jid, username, null);
            logger.info("User {} added successfully.", username);
            return ResponseEntity.ok("Contact added successfully.");

        } catch (XMPPErrorException e) {
            logger.error("XMPP Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("XMPP Error: " + e.getMessage());
        } catch (SmackException | InterruptedException | XMPPException e) {
            logger.error("Error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
