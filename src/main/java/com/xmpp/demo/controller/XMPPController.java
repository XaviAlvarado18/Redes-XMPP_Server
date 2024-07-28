package com.xmpp.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xmpp")
public class XMPPController {

    private AbstractXMPPConnection connection;

    @PostMapping("/connect")
    public Map<String, String> connect(@RequestParam("username") String username, @RequestParam("password") String password) {
        Map<String, String> response = new HashMap<>();

        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setXmppDomain("alumchat.lol")
                    .setHost("alumchat.lol") // Usar "alumchat.lol" como host si no se proporciona otro
                    .setPort(5222)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();

            XMPPTCPConnection connection = new XMPPTCPConnection(config);
            connection.connect();
            connection.login();

            response.put("status", "connected");
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            e.printStackTrace(); // Imprimir el stack trace para depuración
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
