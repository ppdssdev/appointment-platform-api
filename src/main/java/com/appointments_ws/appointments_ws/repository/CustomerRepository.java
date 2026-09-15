package com.appointments_ws.appointments_ws.repository;

import com.appointments_ws.appointments_ws.domain.entity.Customer;
import com.appointments_ws.appointments_ws.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByTenantAndPhoneNumber(Tenant tenant, String phoneNumber);
    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndPhoneNumberAndIdNot(UUID tenantId, String phoneNumber, UUID id);
}
