package com.xmpp.demo.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



@RestController
@RequestMapping("/xmpp")
public class GetAvatar {
	
	@Value("${xmpp.domain}")
    private String domain;

	@GetMapping("/get-avatar")
	public Map<String, String> getAvatar(@RequestParam("username") String username, HttpServletRequest request) {
	    Map<String, String> response = new HashMap<>();
	    HttpSession session = request.getSession();
	    XMPPTCPConnection connection = (XMPPTCPConnection) session.getAttribute("xmppConnection");

	    if (connection == null || !connection.isConnected() || !connection.isAuthenticated()) {
	        response.put("status", "error");
	        response.put("message", "User is not authenticated. Please log in first.");
	        return response;
	    }

	    try {
	        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
	        EntityBareJid jid = JidCreate.entityBareFrom(username + "@" + domain);
	        VCard vCard = vCardManager.loadVCard(jid);
	        byte[] avatarBytes = vCard.getAvatar();

	        if (avatarBytes != null) {
	            String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
	            response.put("status", "success");
	            response.put("avatar", base64Avatar);
	        } else {
	            response.put("status", "no_avatar");
	        }
	    } catch (XMPPException | SmackException | IOException | InterruptedException e) {
	        response.put("status", "error");
	        response.put("message", e.getMessage());
	    }

	    return response;
	}

}
