package org.fontory.fontorybe.sms.application.port;

public interface SmsService {
    void sendFontCreationNotification(String to, String fontName);
    void sendFontProgressNotification(String to, String fontName);
}
