package antifraud.DTO;

import jakarta.validation.constraints.NotBlank;

public record TransactionFeedbackDTO(long transactionId, @NotBlank String feedback) {
}
