package com.aziarets.vividapp.model;

import org.springframework.security.core.GrantedAuthority;

public enum BotUserRole implements GrantedAuthority {
    ADMIN("ADMIN"),
    USER("USER");

    private String role;

    BotUserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getAuthority() {
        return role;
    }
}

