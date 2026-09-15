package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.CustomerRequest;
import com.appointments_ws.appointments_ws.domain.dto.CustomerResponse;
import com.appointments_ws.appointments_ws.domain.entity.Customer;
import com.appointments_ws.appointments_ws.domain.entity.Tenant;
import com.appointments_ws.appointments_ws.domain.exception.ConflictException;
import com.appointments_ws.appointments_ws.domain.exception.ResourceNotFoundException;
import com.appointments_ws.appointments_ws.repository.CustomerRepository;
import com.appointments_ws.appointments_ws.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final TenantRepository tenants;

    public CustomerService(CustomerRepository customers, TenantRepository tenants) {
        this.customers = customers;
        this.tenants = tenants;
    }

    @Transactional
    public CustomerResponse create(UUID tenantId, CustomerRequest request) {
        Tenant tenant = tenants.findById(tenantId).filter(Tenant::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active tenant not found"));
        customers.findByTenantAndPhoneNumber(tenant, request.phoneNumber()).ifPresent(existing -> {
            throw new ConflictException("A customer with this phone number already exists");
        });
        Customer customer = new Customer();
        customer.setTenant(tenant);
        apply(customer, request);
        return response(customers.save(customer));
    }

    @Transactional
    public CustomerResponse update(UUID tenantId, UUID id, CustomerRequest request) {
        Customer customer = customers.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customers.existsByTenantIdAndPhoneNumberAndIdNot(tenantId, request.phoneNumber(), id)) {
            throw new ConflictException("A customer with this phone number already exists");
        }
        apply(customer, request);
        return response(customer);
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setFullName(request.fullName().trim());
        customer.setPhoneNumber(request.phoneNumber());
    }

    static CustomerResponse response(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getPhoneNumber(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
