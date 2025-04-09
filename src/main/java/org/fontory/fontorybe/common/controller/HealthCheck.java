package org.fontory.fontorybe.common.controller;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheck {
    private final FontRequestProducer fontRequestProducer;

    @Value("${commit.hash}")
    public String commitHash;

    @GetMapping("/health-check")
    public String healthCheck() { return commitHash; }

    @GetMapping("sqs-test")
    public String sqsTest() {
        FontRequestProduceDto dto = FontRequestProduceDto.builder()
                .memberId(1L)
                .fontId(1L)
                .fontName("testFontName")
                .templateURL("https://fontory-profile-image.s3.ap-northeast-2.amazonaws.com/a88cd9a0-36fb-4a4f-abd8-696e6235a0a9.jpeg")
                .author("testMemberNickname")
                .requestUUID(MDC.get("requestId"))
                .build();
        fontRequestProducer.sendFontRequest(dto);
        return "test";
    }
}
