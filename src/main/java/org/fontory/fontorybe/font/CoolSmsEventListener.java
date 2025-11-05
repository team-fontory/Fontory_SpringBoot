package org.fontory.fontorybe.font;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.sms.application.port.PhoneNumberStorage;
import org.fontory.fontorybe.sms.application.port.SmsService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoolSmsEventListener {

    private final PhoneNumberStorage phoneNumberStorage;
    private final SmsService smsService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFontCreateRequestNotificationAndSavePhoneNumber(FontCreateRequestNotificationEvent e) {
        Font font = e.getFont();
        String phoneNumber = e.getPhoneNumber();

        log.info("sms send start & save phone number in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);

        phoneNumberStorage.savePhoneNumber(font, phoneNumber);
        smsService.sendFontCreateRequestNotification(phoneNumber, font.getName());

        log.info("sms sent & phone number saved in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendFontCreateCompleteNotificationAndRemovePhoneNumber(FontCreateCompleteNotificationEvent e) {
        Font font = e.getFont();
        String phoneNumber = phoneNumberStorage.getPhoneNumber(font);

        log.info("sms send start & save phone number in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);

        smsService.sendFontCreateCompleteNotification(phoneNumber, font.getName());
        phoneNumberStorage.removePhoneNumber(font);

        log.info("sms sent & phone number saved in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void resendFontCreateRequestSavePhoneNumber(FontCreateRequestNotificationEvent e) {
        Font font = e.getFont();
        String phoneNumber = phoneNumberStorage.getPhoneNumber(font);
        log.info("save phone number in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);

        phoneNumberStorage.removePhoneNumber(font);

        log.info("phone number saved in redis - fontId={}, phoneNumber={}", font.getId(), phoneNumber);
    }
}
