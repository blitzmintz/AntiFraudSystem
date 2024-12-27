package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record SuspiciousIPRequestDTO(@NotBlank String ip) {
}
