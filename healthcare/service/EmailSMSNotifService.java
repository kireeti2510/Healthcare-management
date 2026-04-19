package com.healthcare.service;

import com.healthcare.model.NotificationDelivery;
import com.healthcare.model.NotificationDelivery.Channel;

public class EmailSMSNotifService implements INotificationService {
    private final String emailClient;
    private final String smsClient;
    private final int maxRetries;
    private final long baseBackoffMillis;

    public EmailSMSNotifService(String emailClient, String smsClient) {
        this(emailClient, smsClient, 3, 150);
    }

    public EmailSMSNotifService(String emailClient, String smsClient, int maxRetries, long baseBackoffMillis) {
        this.emailClient = emailClient;
        this.smsClient = smsClient;
        this.maxRetries = Math.max(0, maxRetries);
        this.baseBackoffMillis = Math.max(0, baseBackoffMillis);
    }

    @Override
    public void sendEmail(String to, String msg) {
        System.out.printf("[EMAIL via %s] To: %s | %s%n", emailClient, to, msg);
    }

    @Override
    public void sendSMS(String to, String msg) {
        System.out.printf("[SMS via %s] To: %s | %s%n", smsClient, to, msg);
    }

    @Override
    public NotificationDelivery notify(String event, String recipient) {
        NotificationDelivery delivery = new NotificationDelivery(
                event,
                recipient,
                resolveChannel(recipient),
                maxRetries
        );

        System.out.println("[NOTIFICATION] queued event=" + event + " status=" + delivery.getStatus());
        while (true) {
            try {
                dispatch(delivery, event);
                delivery.markSent();
                System.out.println("[NOTIFICATION] delivered status=" + delivery.getStatus()
                        + " retries=" + delivery.getRetryCount());
                return delivery;
            } catch (RuntimeException ex) {
                delivery.markFailed(ex.getMessage());
                System.out.println("[NOTIFICATION] delivery failed status=" + delivery.getStatus()
                        + " reason=" + delivery.getFailureReason()
                        + " retries=" + delivery.getRetryCount());

                if (!delivery.canRetry()) {
                    delivery.markAbandoned();
                    System.out.println("[NOTIFICATION] abandoned status=" + delivery.getStatus());
                    return delivery;
                }

                delivery.markRetrying();
                long waitMillis = Math.max(0, baseBackoffMillis * delivery.getRetryCount());
                System.out.println("[NOTIFICATION] retrying in " + waitMillis + "ms");
                sleep(waitMillis);
            }
        }
    }

    private void dispatch(NotificationDelivery delivery, String event) {
        if (delivery.getRecipient() == null || delivery.getRecipient().isBlank()) {
            throw new IllegalStateException("No notification recipient.");
        }

        if (delivery.getChannel() == Channel.EMAIL) {
            sendEmail(delivery.getRecipient(), event);
            return;
        }
        if (delivery.getChannel() == Channel.SMS) {
            sendSMS(delivery.getRecipient(), event);
            return;
        }

        throw new IllegalStateException("No channel selected.");
    }

    private Channel resolveChannel(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return Channel.NONE;
        }
        return recipient.contains("@") ? Channel.EMAIL : Channel.SMS;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry backoff interrupted", e);
        }
    }
}
