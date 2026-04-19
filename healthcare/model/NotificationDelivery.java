package com.healthcare.model;

import com.healthcare.model.enums.NotificationStatus;

import java.time.LocalDateTime;

public class NotificationDelivery {
    public enum Channel { EMAIL, SMS, NONE }

    private final String event;
    private final String recipient;
    private final Channel channel;
    private final int maxRetries;

    private NotificationStatus status;
    private int retryCount;
    private String failureReason;
    private LocalDateTime deliveredAt;

    public NotificationDelivery(String event, String recipient, Channel channel, int maxRetries) {
        this.event = event;
        this.recipient = recipient;
        this.channel = channel;
        this.maxRetries = Math.max(0, maxRetries);
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
    }

    public void markRetrying() {
        this.status = NotificationStatus.RETRYING;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAbandoned() {
        this.status = NotificationStatus.ABANDONED;
    }

    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    public String getEvent() { return event; }
    public String getRecipient() { return recipient; }
    public Channel getChannel() { return channel; }
    public int getMaxRetries() { return maxRetries; }
    public NotificationStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
}
