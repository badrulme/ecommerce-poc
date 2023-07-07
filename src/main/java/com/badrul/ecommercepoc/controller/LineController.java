package com.badrul.ecommercepoc.controller;

import com.badrul.ecommercepoc.service.LineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("line")
public class LineController {
    private final LineService service;


    @PostMapping(value = "webhook/callback", consumes = {"application/json"},
            produces = {"application/json"})
    public void handleLineWebhookRequest() {
        service.handleLineWebhookRequest();
    }
}
