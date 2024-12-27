package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserResponseDTO(@NotBlank long id, @NotBlank String name, @NotBlank String username, @NotBlank String role) {

}
