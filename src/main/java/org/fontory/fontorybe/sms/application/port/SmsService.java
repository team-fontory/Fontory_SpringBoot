package org.fontory.fontorybe.sms.application.port;

public interface SmsService {
    void sendFontCreationNotification(String to);
    void sendFontProgressNotification(String to);
}
