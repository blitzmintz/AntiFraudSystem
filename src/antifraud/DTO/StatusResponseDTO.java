package antifraud.DTO;
import jakarta.validation.constraints.NotBlank;


public record StatusResponseDTO(@NotBlank String status) {
}
