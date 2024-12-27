package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record StolenCardRequestDTO(@NotBlank String number) {
}
