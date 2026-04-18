package com.healthcare.model;

import com.healthcare.model.enums.Role;
import java.util.UUID;

public abstract class User {
    protected UUID userId;
    protected String email;
    protected String passwordHash;
    protected Role role;

    public User(UUID userId, String email, String passwordHash, Role role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public abstract boolean authenticate(String password);
    public abstract boolean hasPermission(String action);

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
}
