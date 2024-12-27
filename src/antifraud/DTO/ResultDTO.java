package antifraud.DTO;

import antifraud.Enums.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private TransactionResponse result;
    private String info;


}
