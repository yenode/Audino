package com.audino.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single audit log entry tracking a CRUD operation.
 */
public class AuditLog {

    private long id;
    private LocalDateTime timestamp;
    private String username;
    private String operation;  // CREATE, READ, UPDATE, DELETE
    private String entityType; // PATIENT, MEDICATION, PRESCRIPTION, PASSWORD
    private String entityId;
    private String details;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {}

    public AuditLog(String username, String operation, String entityType, String entityId, String details) {
        this.timestamp = LocalDateTime.now();
        this.username = username;
        this.operation = operation;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(DISPLAY_FMT) : "";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s %s (%s) — %s",
                getFormattedTimestamp(), username, operation, entityType, entityId, details);
    }
}
