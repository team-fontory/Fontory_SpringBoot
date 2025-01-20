package org.fontory.fontorybe.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthCheck {

    @Value("${commit.hash}")
    public String commitHash;

    @ResponseBody
    @GetMapping("/health-check")
    public String healthCheck() { return commitHash; }
}
