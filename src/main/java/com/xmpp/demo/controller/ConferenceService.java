package com.xmpp.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConferenceService {

    private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);

    public static void createConference(AbstractXMPPConnection connection, GroupRequest groupRequest) throws Exception {
        MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);

        String groupName = groupRequest.getgroupName();
        String conferenceJid = groupName + "@conference.alumchat.lol";

        List<String> members = groupRequest.getMembers();

        // Obtener o crear la sala de conferencia
        MultiUserChat muc = mucManager.getMultiUserChat(JidCreate.entityBareFrom(conferenceJid));

        // Crear o unirse a la sala de conferencia
        muc.createOrJoin(Resourcepart.from(groupName)); 

        // Obtener el formulario de configuración de la sala
        Form configForm = muc.getConfigurationForm();

        // Crear un formulario rellenable
        FillableForm fillableConfigForm = configForm.getFillableForm();

        // Establecer los valores deseados en el formulario de configuración
        fillableConfigForm.setAnswer("muc#roomconfig_persistentroom", "true");
        fillableConfigForm.setAnswer("muc#roomconfig_publicroom", "true");

        // Enviar el formulario de configuración
        muc.sendConfigurationForm(fillableConfigForm);

        // Enviar invitaciones a los miembros
        for (String member : members) {
            muc.invite(JidCreate.entityBareFrom(member), "Join the conference");
        }


        // Esperar un momento para asegurar que la sala esté disponible
        Thread.sleep(2000); // Espera de 2 segundos, ajustar si es necesario

        // Verificar la presencia del propio usuario en la sala
        EntityFullJid selfJid = (EntityFullJid) connection.getUser();
        logger.info("User JID: {}", selfJid);

        Presence selfPresence = muc.getOccupantPresence(selfJid);
        if (selfPresence == null) {
            logger.warn("Presence for user {} is null", selfJid);
        } else {
            logger.info("Presence for user {}: {}", selfJid, selfPresence.getType());
        }

        if (selfPresence == null || selfPresence.getType() != Presence.Type.available) {
            muc.join(Resourcepart.from(groupName));
            logger.info("Uniendome a la sala");
        }

    }

    public List<String> getJoinedGroups(AbstractXMPPConnection connection, String username) throws Exception {
        MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);
    
        logger.info("mucManager: {}", mucManager);
    
        // Obtener los JIDs de las salas de conferencia a las que el usuario está conectado
        Set<EntityBareJid> joinedRooms = mucManager.getJoinedRooms();
        List<String> groupJids = new ArrayList<>();
    
        logger.info("JIDS: {}", joinedRooms);
    
        for (EntityBareJid roomJid : joinedRooms) {
            MultiUserChat muc = mucManager.getMultiUserChat(roomJid);
    
            // Convertir el nickname en Resourcepart
            Resourcepart resourcePart = Resourcepart.from(username);
            EntityFullJid fullJid = JidCreate.entityFullFrom(roomJid, resourcePart);
    
            // Verificar la presencia del usuario en la sala
            Presence occupantPresence = muc.getOccupantPresence(fullJid);
    
            if (occupantPresence != null && occupantPresence.getType() == Presence.Type.available) {
                logger.info("roomJid: {}", roomJid);
                groupJids.add(roomJid.toString());
            }
        }
    
        return groupJids;
    }
}
