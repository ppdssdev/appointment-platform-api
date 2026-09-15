package com.appointments_ws.appointments_ws;

import com.appointments_ws.appointments_ws.domain.dto.*;
import com.appointments_ws.appointments_ws.domain.exception.ConflictException;
import com.appointments_ws.appointments_ws.repository.AppointmentRepository;
import com.appointments_ws.appointments_ws.repository.CustomerRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalAvailabilityRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalRepository;
import com.appointments_ws.appointments_ws.service.AppointmentService;
import com.appointments_ws.appointments_ws.service.AvailabilityService;
import com.appointments_ws.appointments_ws.service.CustomerService;
import com.appointments_ws.appointments_ws.service.ProfessionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class AppointmentIntegrationTest {
    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("appointments").withUsername("postgres").withPassword("postgres");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired CustomerService customerService;
    @Autowired ProfessionalService professionalService;
    @Autowired AvailabilityService availabilityService;
    @Autowired AppointmentService appointmentService;
    @Autowired AppointmentRepository appointments;
    @Autowired ProfessionalAvailabilityRepository availability;
    @Autowired CustomerRepository customers;
    @Autowired ProfessionalRepository professionals;

    private CustomerResponse customer;
    private ProfessionalResponse professional;
    private Instant mondayAtTen;

    @BeforeEach
    void setUp() {
        appointments.deleteAll();
        availability.deleteAll();
        customers.deleteAll();
        professionals.deleteAll();
        customer = customerService.create(TENANT_ID, new CustomerRequest("Ada Lovelace", "15555550101"));
        professional = professionalService.create(TENANT_ID, new ProfessionalRequest("Grace Hopper", true, "UTC"));
        availabilityService.define(TENANT_ID, professional.id(),
                new AvailabilityRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        mondayAtTen = Instant.now().plusSeconds(8 * 24 * 3600).atZone(java.time.ZoneOffset.UTC)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).withHour(10).withMinute(0).withSecond(0).withNano(0).toInstant();
    }

    @Test
    void schedulesIdempotentlyAndRejectsOverlap() {
        ScheduleAppointmentRequest request = request(mondayAtTen, mondayAtTen.plusSeconds(3600));
        AppointmentResponse first = appointmentService.schedule(TENANT_ID, "schedule-1", request);
        AppointmentResponse repeated = appointmentService.schedule(TENANT_ID, "schedule-1", request);

        assertThat(repeated.id()).isEqualTo(first.id());
        assertThatThrownBy(() -> appointmentService.schedule(TENANT_ID, "schedule-2",
                request(mondayAtTen.plusSeconds(1800), mondayAtTen.plusSeconds(5400))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void confirmsCancelsAndReschedules() {
        AppointmentResponse scheduled = appointmentService.schedule(TENANT_ID, "lifecycle-1",
                request(mondayAtTen, mondayAtTen.plusSeconds(3600)));
        assertThat(appointmentService.confirm(TENANT_ID, scheduled.id()).status().name()).isEqualTo("CONFIRMED");
        assertThat(appointmentService.cancel(TENANT_ID, scheduled.id()).status().name()).isEqualTo("CANCELLED");

        AppointmentResponse second = appointmentService.schedule(TENANT_ID, "lifecycle-2",
                request(mondayAtTen.plusSeconds(7200), mondayAtTen.plusSeconds(10800)));
        AppointmentResponse moved = appointmentService.reschedule(TENANT_ID, second.id(),
                new RescheduleAppointmentRequest(mondayAtTen.plusSeconds(10800), mondayAtTen.plusSeconds(14400)));
        assertThat(moved.startsAt()).isEqualTo(mondayAtTen.plusSeconds(10800));
    }

    private ScheduleAppointmentRequest request(Instant start, Instant end) {
        return new ScheduleAppointmentRequest(customer.id(), professional.id(), start, end);
    }
}
