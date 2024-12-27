package antifraud.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity(name = "card")
@Getter
@Setter
@NoArgsConstructor
public class Card implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column
    private String number;
    @Column
    @JsonIgnore
    private Long maxAllowed;
    @Column
    @JsonIgnore
    private Long maxManual;

    public Card(String number) {
        this.number = number;
        maxAllowed = 200L;
        maxManual = 1500L;
    }

}
