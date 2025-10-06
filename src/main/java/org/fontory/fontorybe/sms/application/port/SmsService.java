package org.fontory.fontorybe.sms.application.port;

public interface SmsService {
    void sendFontCreateRequestNotification(String to, String fontName);
    void sendFontCreateCompleteNotification(String to, String fontName);
}
