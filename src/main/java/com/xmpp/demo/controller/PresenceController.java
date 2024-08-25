package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/xmpp")
@CrossOrigin(origins = "http://localhost:4200")
public class PresenceController {

    private static final Logger logger = LoggerFactory.getLogger(PresenceController.class);

    
    @GetMapping("/get-current-status")
    public Map<String, String> getCurrentStatus(HttpSession session) {
        Map<String, String> response = new HashMap<>();
        AbstractXMPPConnection connection = (AbstractXMPPConnection) session.getAttribute("xmppConnection");

        if (connection == null) {
            response.put("status", "no connection found in session");
            return response;
        }

        try {
            Roster roster = Roster.getInstanceFor(connection);
            EntityBareJid userJid = connection.getUser().asEntityBareJid();
            Presence presence = roster.getPresence(userJid);

            String status = presence.getStatus();

            if (status == null) {
                // Si el estado es nulo, asignar en función de la disponibilidad o tipo de presencia
                if (presence.getMode() == Presence.Mode.away) {
                    status = "absent";
                } else if (presence.getMode() == Presence.Mode.dnd) {
                    status = "busy";
                } else if (presence.getMode() == Presence.Mode.xa) {
                    status = "unavailable";
                } else if (presence.isAvailable()) {
                    status = "available";
                } else {
                    status = "offline";
                }
            }

            response.put("currentStatus", status);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }


    
    @PostMapping("/change-status")
    public Map<String, String> changeStatus(@RequestParam("status") String status, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        XMPPTCPConnection connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

        if (connection == null) {
            response.put("status", "no connection found in session");
            return response;
        }

        try {
            Presence presence = new Presence(Presence.Type.available);

            // Dependiendo del estado recibido, cambiamos el Presence.Mode
            switch (status.toLowerCase()) {
                case "available":
                    presence.setMode(Presence.Mode.available);
                    presence.setStatus("I'm available");
                    break;
                case "absent":
                    presence.setMode(Presence.Mode.away);
                    presence.setStatus("I'm away");
                    break;
                case "unavailable":
                    presence.setMode(Presence.Mode.xa); // Extended Away
                    presence.setStatus("I'm unavailable");
                    break;
                case "busy":
                    presence.setMode(Presence.Mode.dnd); // Do Not Disturb
                    presence.setStatus("I'm busy");
                    break;
                case "offline":
                    presence = new Presence(Presence.Type.unavailable);
                    presence.setStatus("I'm offline");
                    break;
                default:
                    response.put("status", "invalid status");
                    return response;
            }

            // Enviar el nuevo Presence al servidor
            connection.sendStanza(presence);

            response.put("status", "status changed to " + status);
        } catch (Exception e) {
            logger.error("Failed to change status", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }

        return response;
    }
}
