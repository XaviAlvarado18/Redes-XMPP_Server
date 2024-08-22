package com.xmpp.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.File;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConferenceService {

    private static final Logger logger = LoggerFactory.getLogger(XMPPController.class);
    
    private static final String GROUPS_JSON_FILE = "groupRequest.json";

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

        // Serializar la información del grupo a JSON y guardarla en un archivo
        saveGroupRequestToJson(groupRequest);
    }

    private static void saveGroupRequestToJson(GroupRequest groupRequest) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("groupRequest.json");
        objectMapper.writeValue(file, groupRequest);
    }

    
    public List<GroupRequest> getGroupsForUser(String username) throws Exception {
        
    	logger.info("Este es (ConferenceService): {}", username);
    	// Leer el archivo JSON
        ObjectMapper objectMapper = new ObjectMapper();
        
        GroupRequest[] groups = objectMapper.readValue(new File(GROUPS_JSON_FILE), GroupRequest[].class);

        logger.info("Contenido del JSON (ConferenceService): {}", groups);
        
        // Filtrar los grupos donde el usuario es miembro
        return List.of(groups).stream()
                .filter(group -> group.getMembers().contains(username))
                .collect(Collectors.toList());
    }
    
}
