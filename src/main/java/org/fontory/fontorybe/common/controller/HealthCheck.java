package org.fontory.fontorybe.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @Value("${commit.hash}")
    public String commitHash;

    @GetMapping("/health-check")
    public String healthCheck() { return commitHash; }
}
