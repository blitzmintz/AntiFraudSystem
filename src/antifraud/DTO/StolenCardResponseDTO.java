package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record StolenCardResponseDTO(@NotBlank Long id, @NotBlank String number) {
}
