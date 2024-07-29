package com.xmpp.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.*;
import jakarta.servlet.http.*;


@RestController
@RequestMapping("/xmpp")
public class XMPPController {

	private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);
    private AbstractXMPPConnection connection;

    @Value("${xmpp.domain}")
    private String domain;
    
    @Value("${xmpp.host}")
    private String host;
    
    @Value("${xmpp.port}")
    private int port;
    
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/connect")
    public Map<String, String> connect(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        
        logger.info("Username: {}", username);
        logger.info("Password: {}", password);
        
        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setXmppDomain(domain)
                    .setHost(host) 
                    .setPort(port)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                    .build();

            XMPPTCPConnection connection = new XMPPTCPConnection(config);
            connection.connect();
            connection.login();

            HttpSession session = request.getSession();
            session.setAttribute("xmppConnection", connection); // Guardar la conexión en la sesión

            response.put("status", "connected");
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            logger.error("Connection failed: ", e);
            response.put("status", "connection_failed");
            response.put("error", e.getMessage());
        }

        return response;
    }

    

    @PostMapping("/disconnect")
    public Map<String, String> disconnect() {
        Map<String, String> response = new HashMap<>();
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
            response.put("status", "disconnected");
        } else {
            response.put("status", "not connected");
        }
        return response;
    }
}
