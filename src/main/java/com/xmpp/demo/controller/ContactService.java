package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    public List<ContactXMPP> getContacts(String username, AbstractXMPPConnection connection) {
        List<ContactXMPP> contactsList = new ArrayList<>();

        try {
            if (connection != null) {
                Roster roster = Roster.getInstanceFor(connection);

                // Ensure the roster is loaded
                roster.reloadAndWait();

                VCardManager vCardManager = VCardManager.getInstanceFor(connection);
                
             // Get all entries in the roster
                for (RosterEntry entry : roster.getEntries()) {
                    Jid jid = entry.getJid();

                    // Check if the Jid is an instance of EntityBareJid
                    if (jid instanceof EntityBareJid) {
                        EntityBareJid bareJid = (EntityBareJid) jid;
                        String contactUsername = bareJid.asEntityBareJidString();

                        // Obtener el nombre completo (nombre de entrada en el roster)
                        String fullName = entry.getName();

                        // Obtener el estado del contacto
                        Presence presence = roster.getPresence(bareJid);
                        boolean isAvailable = presence.isAvailable();
                        String status = presence.getStatus();
                        
                        if (status == null) {
                            // Asigna un valor por defecto basado en la disponibilidad
                            status = isAvailable ? "Disponible" : "Desconectado";
                        }
                        
                        
                        contactsList.add(new ContactXMPP(contactUsername, status, fullName));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get contacts for user: {}", username, e);
        }

        return contactsList;
    }


    private AbstractXMPPConnection getConnectionForUser(String username) {
        // Implement logic to retrieve the user's XMPP connection
        // This is a placeholder method and should be replaced with actual logic
        return null;
    }
}
