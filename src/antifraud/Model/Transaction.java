package antifraud.Model;
import antifraud.DTO.*;
import antifraud.Enums.Region;
import antifraud.Enums.TransactionResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TRANSACTION")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(value = "transactionId")
    private Long id;

    @NotNull
    private Long amount;

    @NotNull
    private String ip;

    @NotNull
    private String number;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @JsonProperty(value = "result")
    private TransactionResponse status;

    @Enumerated(EnumType.STRING)
    private TransactionResponse feedback;


    public Transaction(Long amount, String ip, String number, Region region, LocalDateTime date, TransactionResponse status, TransactionResponse feedback) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.status = status;
        this.feedback = null;

    }

}
