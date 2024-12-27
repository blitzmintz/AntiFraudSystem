package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(@NotBlank String username, @NotBlank String operation) {
}
