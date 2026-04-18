package com.healthcare.controller;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.User;
import com.healthcare.pattern.creational.UserFactory;
import com.healthcare.model.enums.Role;
import com.healthcare.repository.IAuditLogService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * MVC Controller - Authentication
 * Member: Kireeti Reddy P (PES1UG23CS307)
 */
public class AuthController {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MILLIS = 2 * 60 * 1000;

    private final IAuditLogService auditLog;
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Long> lockedUntilByEmail = new HashMap<>();
    private String lastAuthMessage = "Ready.";

    public AuthController(IAuditLogService auditLog) { this.auditLog = auditLog; }

    public boolean login(User user, String password) {
        if (user == null || user.getEmail() == null || password == null) {
            auditLog.log("LOGIN_FAILED", null);
            lastAuthMessage = "Invalid credentials.";
            return false;
        }

        String email = user.getEmail().trim().toLowerCase();
        long now = System.currentTimeMillis();
        long lockedUntil = lockedUntilByEmail.getOrDefault(email, 0L);
        if (lockedUntil > now) {
            long secondsLeft = Math.max(1L, (lockedUntil - now + 999L) / 1000L);
            lastAuthMessage = "Invalid credentials. Too many failed attempts. Try again in "
                    + secondsLeft + "s.";
            auditLog.log("LOGIN_BLOCKED", null);
            return false;
        }

        Optional<User> persisted = findByEmail(email);
        boolean ok = persisted.map(u -> u.authenticate(password)).orElse(false);
        auditLog.log(ok ? "LOGIN_SUCCESS" : "LOGIN_FAILED", persisted.map(User::getUserId).orElse(null));

        if (ok) {
            failedAttempts.put(email, 0);
            lockedUntilByEmail.remove(email);
            lastAuthMessage = "Login successful.";
            return true;
        }

        int attemptCount = failedAttempts.getOrDefault(email, 0) + 1;
        if (attemptCount >= MAX_LOGIN_ATTEMPTS) {
            failedAttempts.put(email, 0);
            lockedUntilByEmail.put(email, now + LOCK_DURATION_MILLIS);
            lastAuthMessage = "Invalid credentials. Too many failed attempts. Account locked for 2 minutes.";
            return false;
        }

        failedAttempts.put(email, attemptCount);
        int attemptsLeft = MAX_LOGIN_ATTEMPTS - attemptCount;
        lastAuthMessage = "Invalid credentials. " + attemptsLeft + " attempt(s) left.";
        return ok;
    }

    public String getLastAuthMessage() {
        return lastAuthMessage;
    }

    private Optional<User> findByEmail(String email) {
        String sql = "SELECT user_id, email, password_hash, role FROM users WHERE email = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Role role = Role.valueOf(rs.getString("role"));
                UUID id = UUID.fromString(rs.getString("user_id"));
                String storedEmail = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                return Optional.of(UserFactory.createUser(role, id, storedEmail, passwordHash, null, null));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Auth lookup failed for email: " + email, e);
        }
    }
}
