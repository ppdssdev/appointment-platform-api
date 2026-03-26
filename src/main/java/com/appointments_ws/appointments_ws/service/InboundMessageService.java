package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.InboundMessageRequest;
import com.appointments_ws.appointments_ws.domain.dto.InboundMessageResponse;

public interface InboundMessageService {

    public InboundMessageResponse process(InboundMessageRequest request);

}
