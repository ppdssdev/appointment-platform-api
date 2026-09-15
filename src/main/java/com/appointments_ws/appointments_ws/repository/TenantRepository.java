package com.appointments_ws.appointments_ws.repository;

import com.appointments_ws.appointments_ws.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
