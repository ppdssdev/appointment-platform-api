package com.appointments_ws.appointments_ws.repository;

import com.appointments_ws.appointments_ws.domain.entity.WhatsAppChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppChannelRepository extends JpaRepository<WhatsAppChannel, UUID> {
    Optional<WhatsAppChannel> findByPhoneNumberAndActiveTrue(String phoneNumber);
}