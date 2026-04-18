package com.healthcare.service;

public interface INotificationService {
    void sendEmail(String to, String msg);
    void sendSMS(String to, String msg);
    void notify(String event);
}
