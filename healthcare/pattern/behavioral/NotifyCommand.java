package com.healthcare.pattern.behavioral;

import com.healthcare.service.INotificationService;

public class NotifyCommand implements Command {
    private final INotificationService notifSvc;
    private final String message;
    private final String recipient;

    public NotifyCommand(INotificationService notifSvc, String message, String recipient) {
        this.notifSvc = notifSvc;
        this.message = message;
        this.recipient = recipient;
    }

    @Override
    public void execute() { notifSvc.sendEmail(recipient, message); }

    @Override
    public void undo() { notifSvc.sendEmail(recipient, "REVERTED: " + message); }
}
