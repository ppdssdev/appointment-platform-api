package com.appointments_ws.appointments_ws.controller;


import com.appointments_ws.appointments_ws.domain.dto.InboundMessageRequest;
import com.appointments_ws.appointments_ws.domain.dto.InboundMessageResponse;
import com.appointments_ws.appointments_ws.service.InboundMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound-messages")
public class InboundMessageController {

    private final InboundMessageService inboundMessageService;

    public InboundMessageController(InboundMessageService inboundMessageService) {
        this.inboundMessageService = inboundMessageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public InboundMessageResponse receive(@Valid @RequestBody InboundMessageRequest request) {
        return inboundMessageService.process(request);
    }
}