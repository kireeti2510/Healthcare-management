package com.healthcare.service;

public class EmailSMSNotifService implements INotificationService {
    private final String emailClient;
    private final String smsClient;

    public EmailSMSNotifService(String emailClient, String smsClient) {
        this.emailClient = emailClient;
        this.smsClient = smsClient;
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
    public void notify(String event) {
        System.out.println("[NOTIFICATION] " + event);
    }
}
