package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequestDTO(@NotBlank String username, @NotBlank String role) {
}
