package antifraud.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "suspicious_ip")
public class IP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true, nullable = false)
    private String ip;

    public IP(){}

    public IP(String ip){
        this.ip = ip;
    }
}
