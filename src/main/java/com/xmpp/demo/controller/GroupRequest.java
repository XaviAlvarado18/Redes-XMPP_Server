package com.xmpp.demo.controller;

import java.util.List;

public class GroupRequest {
    private String groupName; // Nombre del grupo
    private List<String> members; // Lista de usuarios en el grupo

    // Constructor por defecto (necesario para Jackson)
    public GroupRequest() {
    }

    public GroupRequest(String groupName, List<String> members) {
        this.groupName = groupName;
        this.members = members;
    }

    // Getters y Setters

    public String getgroupName() {
        return this.groupName;
    }

    public void setgroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
