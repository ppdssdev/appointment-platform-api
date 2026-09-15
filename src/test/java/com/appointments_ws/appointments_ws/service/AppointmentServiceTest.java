package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.entity.Appointment;
import com.appointments_ws.appointments_ws.domain.enums.AppointmentStatus;
import com.appointments_ws.appointments_ws.domain.exception.BusinessRuleException;
import com.appointments_ws.appointments_ws.repository.AppointmentRepository;
import com.appointments_ws.appointments_ws.repository.CustomerRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalAvailabilityRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointments;
    @Mock CustomerRepository customers;
    @Mock ProfessionalRepository professionals;
    @Mock ProfessionalAvailabilityRepository availability;
    @InjectMocks AppointmentService service;

    @Test
    void doesNotConfirmACancelledAppointment() {
        UUID tenantId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointments.findByIdAndTenantId(appointmentId, tenantId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.confirm(tenantId, appointmentId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only pending appointments can be confirmed");
    }
}
