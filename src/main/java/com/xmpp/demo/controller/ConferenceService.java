package com.xmpp.demo.controller;

import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.springframework.stereotype.Service;

@Service
public class ConferenceService {
    public static void createConference(AbstractXMPPConnection connection, GroupRequest groupRequest) throws Exception {
        MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(connection);

        String groupName = groupRequest.getgroupName();
        String conferenceJid = groupName + "@conference.alumchat.lol";

        List<String> members = groupRequest.getMembers();

        // Crear o unirse a la sala de conferencia
        MultiUserChat muc = mucManager.getMultiUserChat(JidCreate.entityBareFrom(conferenceJid));

        muc.createOrJoin(Resourcepart.from(groupName)); // Crea la sala si no existe o se une a ella si ya existe

        // Obtener el formulario de configuración de la sala
        Form configForm = muc.getConfigurationForm();

        
        //DataForm configDataForm = new DataForm(configForm);
        
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
    }
}
