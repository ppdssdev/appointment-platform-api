package com.appointments_ws.appointments_ws.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfessionalRequest(@NotBlank @Size(max = 150) String fullName, @NotNull Boolean active,
                                  @NotBlank @Size(max = 50) String timeZone) {
}
