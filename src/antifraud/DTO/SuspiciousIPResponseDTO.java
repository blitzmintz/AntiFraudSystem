package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record SuspiciousIPResponseDTO(@NotBlank long id, @NotBlank String ip) {
}
