package com.xmpp.demo.controller;

public class MessageXMPP {
    private String text;
    private String sender;
    private String date_msg;

    public MessageXMPP(String text, String sender, String date_msg) {
        this.text = text;
        this.sender = sender;
        this.date_msg = date_msg;
    }

    // Getters y setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDate_msg() {
        return date_msg;
    }

    public void setDay_msg(String date_msg) {
        this.date_msg = date_msg;
    }
}
