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
    private String fromPhoneNumber;

    private final DefaultMessageService messageService;

    private static final String FONT_DETAILS_URL = "https://fontory.co.kr/fonts/%s";
    private static final String FONT_CREATION_MESSAGE_FORMAT = "[Fontory]\n\"%s\" 폰트가 제작중입니다.";
    private static final String FONT_PROGRESS_MESSAGE_FORMAT =
            "[Fontory]\n\"%s\" 폰트 제작이 완료되었습니다.\n" + FONT_DETAILS_URL;

    @Override
    public void sendFontCreateRequestNotification(String to, String fontName) {
        sendSms(to, String.format(FONT_CREATION_MESSAGE_FORMAT, fontName));
    }

    @Override
    public void sendFontCreateCompleteNotification(String to, String fontName, Long fontId) {
        sendSms(to, String.format(FONT_PROGRESS_MESSAGE_FORMAT, fontName,  fontId));
    }

    private void sendSms(String toPhoneNumber, String content) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            log.warn("Service warning: recipient phone number is empty");
            return;
        }

        Message message = new Message();
        message.setFrom(fromPhoneNumber);
        message.setTo(toPhoneNumber);
        message.setText(content);

        messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
