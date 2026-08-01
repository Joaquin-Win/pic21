/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.request.LoginRequest
 *  jakarta.validation.constraints.NotBlank
 */
package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message="El nombre de usuario es obligatorio")
    private @NotBlank(message="El nombre de usuario es obligatorio") String username;
    @NotBlank(message="La contrase\u00f1a es obligatoria")
    private @NotBlank(message="La contrase\u00f1a es obligatoria") String password;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

