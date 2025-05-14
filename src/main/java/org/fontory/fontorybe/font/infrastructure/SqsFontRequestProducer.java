package org.fontory.fontorybe.font.infrastructure;

import io.awspring.cloud.sqs.listener.SqsHeaders;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.font.domain.exception.FontSQSProduceExcepetion;
import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsFontRequestProducer implements FontRequestProducer {

    private final SqsTemplate sqsTemplate;

    @Value("${spring.cloud.aws.sqs.queue-name}")
    private String queueName;

    @Override
    public void sendFontRequest(FontRequestProduceDto fontRequestProduceDto) {
        try {
            log.info("Sending font request to sqs queue {}", queueName);

            Map<String, Object> headers = new HashMap<>();
            headers.put(SqsHeaders.MessageSystemAttributes.SQS_MESSAGE_GROUP_ID_HEADER, generateMessageGroupId(fontRequestProduceDto));

            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .headers(headers)
                    .payload(fontRequestProduceDto));

            log.info("Successfully sent font request to sqs queue {}", queueName);
        } catch (Exception e) {
            log.error("Failed to send font request with sqs queue {}", queueName, e);
            throw new FontSQSProduceExcepetion("Failed to produce font request message with sqs queue " + queueName);
        }
    }

    private String generateMessageGroupId(FontRequestProduceDto dto) {
        return dto.getRequestUUID();
    }
}
