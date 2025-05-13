package org.fontory.fontorybe.sms.application.port;

import org.fontory.fontorybe.font.domain.Font;

public interface PhoneNumberStorage {
    void savePhoneNumber(Font font, String phoneNumber);
    void removePhoneNumber(Font font);
    String getPhoneNumber(Font font);
}
