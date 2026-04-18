package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class AuditLogServiceImpl implements IAuditLogService {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void log(String action, UUID userId) {
        String sql = "INSERT INTO audit_log(action, user_id) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, userId != null ? userId.toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<String> getLog(UUID userId) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT action, timestamp FROM audit_log WHERE user_id=? ORDER BY id DESC LIMIT 100";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) logs.add("[" + rs.getString("timestamp") + "] " + rs.getString("action"));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return logs;
    }
}
