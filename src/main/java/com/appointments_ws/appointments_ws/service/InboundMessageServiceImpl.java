package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.InboundMessageRequest;
import com.appointments_ws.appointments_ws.domain.dto.InboundMessageResponse;
import com.appointments_ws.appointments_ws.domain.entity.Customer;
import com.appointments_ws.appointments_ws.domain.entity.Tenant;
import com.appointments_ws.appointments_ws.domain.entity.WhatsAppChannel;
import com.appointments_ws.appointments_ws.repository.CustomerRepository;
import com.appointments_ws.appointments_ws.repository.WhatsAppChannelRepository;
import com.appointments_ws.appointments_ws.domain.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class InboundMessageServiceImpl implements InboundMessageService {


    private final WhatsAppChannelRepository whatsAppChannelRepository;
    private final CustomerRepository customerRepository;

    public InboundMessageServiceImpl(
            WhatsAppChannelRepository whatsAppChannelRepository,
            CustomerRepository customerRepository
    ) {
        this.whatsAppChannelRepository = whatsAppChannelRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public InboundMessageResponse process(InboundMessageRequest request) {
        WhatsAppChannel channel = whatsAppChannelRepository
                .findByPhoneNumberAndActiveTrue(request.destinationPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp channel not found"));

        Tenant tenant = channel.getTenant();

        Customer customer = customerRepository
                .findByTenantAndPhoneNumber(tenant, request.customerPhoneNumber())
                .orElseGet(() -> createCustomer(tenant, request.customerPhoneNumber()));

        return new InboundMessageResponse(
                tenant.getId(),
                customer.getId(),
                "Mensagem recebida com sucesso",
                "PROCESSED"
        );
    }

    private Customer createCustomer(Tenant tenant, String phoneNumber) {
        Customer customer = new Customer();
        customer.setTenant(tenant);
        customer.setPhoneNumber(phoneNumber);
        customer.setFullName(null);
        return customerRepository.save(customer);
    }
}
