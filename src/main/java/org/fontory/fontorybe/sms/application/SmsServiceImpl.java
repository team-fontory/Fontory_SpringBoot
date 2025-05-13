package org.fontory.fontorybe.sms.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.fontory.fontorybe.sms.application.port.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${coolsms.phone-number}")
    private String phoneNumber;

    private final DefaultMessageService messageService;

    private static final String FONT_CREATION_MESSAGE_FORMAT = "[Fontory]\n\"%s\" 폰트가 제작중입니다.";
    private static final String FONT_PROGRESS_MESSAGE_FORMAT = "[Fontory]\n\"%s\" 폰트 제작이 완료되었습니다.";

    @Override
    public void sendFontCreationNotification(String to, String fontName) {
        sendSms(to, String.format(FONT_CREATION_MESSAGE_FORMAT, fontName));
    }

    @Override
    public void sendFontProgressNotification(String to, String fontName) {
        sendSms(to, String.format(FONT_PROGRESS_MESSAGE_FORMAT, fontName));
    }

    private void sendSms(String to, String text) {
        if (to == null || to.isBlank()) {
            log.warn("Service warning: recipient phone number is empty");
            return;
        }

        Message message = new Message();
        message.setFrom(phoneNumber);
        message.setTo(to);
        message.setText(text);

        messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
