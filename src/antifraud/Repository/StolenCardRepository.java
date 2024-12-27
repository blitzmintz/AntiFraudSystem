package antifraud.Repository;

import antifraud.Model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, String> {
    Optional<StolenCard> findByNumber(String Number);

}
