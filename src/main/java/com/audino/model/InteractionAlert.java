package com.audino.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class InteractionAlert {

    private String alertId;
    private AlertLevel alertLevel;
    private AlertType alertType;
    private String title;
    private String message;
    private String recommendation;
    private String involvedMedications;
    private String patientFactor; 
    private LocalDateTime createdAt;
    private boolean acknowledged;

    public InteractionAlert() {
        // Default constructor for BSON mapping
    }

    public InteractionAlert(AlertLevel alertLevel, AlertType alertType, String title, String message,
                           String recommendation, String involvedMedications, String patientFactor) {
        this.alertId = "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.alertLevel = alertLevel;
        this.alertType = alertType;
        this.title = title;
        this.message = message;
        this.recommendation = recommendation;
        this.involvedMedications = involvedMedications;
        this.patientFactor = patientFactor;
        this.createdAt = LocalDateTime.now();
        this.acknowledged = false;
    }

    public String getCssClass() {
        return switch (alertLevel) {
            case CRITICAL -> "alert-critical";
            case WARNING -> "alert-warning";
            case INFO -> "alert-info";
        };
    }

    public String getFormattedMessage() {
        StringBuilder formatted = new StringBuilder();
        formatted.append("Alert Type: ").append(alertType.getDisplayName());
        formatted.append("\nLevel: ").append(alertLevel.getDisplayName());
        formatted.append("\n\nDetails:\n").append(message);

        if (recommendation != null && !recommendation.trim().isEmpty()) {
            formatted.append("\n\nRecommendation:\n").append(recommendation);
        }
        return formatted.toString();
    }
    
    public void acknowledge() {
        this.acknowledged = true;
    }

    // Getters and Setters for BSON mapping
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public AlertLevel getAlertLevel() { return alertLevel; }
    public void setAlertLevel(AlertLevel alertLevel) { this.alertLevel = alertLevel; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getInvolvedMedications() { return involvedMedications; }
    public void setInvolvedMedications(String involvedMedications) { this.involvedMedications = involvedMedications; }
    public String getPatientFactor() { return patientFactor; }
    public void setPatientFactor(String patientFactor) { this.patientFactor = patientFactor; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractionAlert that = (InteractionAlert) o;
        return Objects.equals(alertId, that.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId);
    }
}