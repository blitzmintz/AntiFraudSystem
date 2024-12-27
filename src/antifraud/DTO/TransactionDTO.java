package antifraud.DTO;

import antifraud.Enums.Region;
import antifraud.Enums.TransactionResponse;
import antifraud.Validation.NullAsEmptyStringSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TransactionDTO(@NotNull @JsonProperty(value = "transactionId") long id, @NotNull Long amount, @NotBlank String ip, @NotBlank String number, Region region,
                             LocalDateTime date, @JsonProperty(value="result") TransactionResponse status,
                             @JsonSerialize(nullsUsing = NullAsEmptyStringSerializer.class) TransactionResponse feedback) {

}
