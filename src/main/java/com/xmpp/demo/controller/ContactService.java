package com.xmpp.demo.controller;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    public List<String> getContacts(String username, AbstractXMPPConnection connection) {
        List<String> contactsList = new ArrayList<>();

        try {
            

            if (connection != null) {
                Roster roster = Roster.getInstanceFor(connection);

                // Ensure the roster is loaded
                roster.reloadAndWait();

                // Get all entries in the roster
                for (RosterEntry entry : roster.getEntries()) {
                    BareJid jid = entry.getJid();
                    contactsList.add(((EntityJid) jid).asEntityBareJidString());
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
