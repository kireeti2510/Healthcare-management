package com.healthcare.service;

import com.healthcare.model.NotificationDelivery;

public interface INotificationService {
    void sendEmail(String to, String msg);
    void sendSMS(String to, String msg);

    default NotificationDelivery notify(String event) {
        return notify(event, null);
    }

    NotificationDelivery notify(String event, String recipient);
}
