package com.xmpp.demo.controller;

public class ContactXMPP {

	private String username;
    private String status;
    private String fullName;
    
    public ContactXMPP(String username, String status, String fullName) {
        this.username = username;
        this.status = status;
        this.fullName = fullName;
    }

 // Getters y setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullname() {
        return fullName;
    }
    
    public void setFullname(String fullname) {
        this.fullName = fullname;
    }
	
}
