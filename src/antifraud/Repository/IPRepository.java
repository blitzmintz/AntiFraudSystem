package antifraud.Repository;

import antifraud.Model.IP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPRepository extends JpaRepository<IP, String> {
    Optional<IP> findByIp(String ip);
}
