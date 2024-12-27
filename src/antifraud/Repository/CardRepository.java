package antifraud.Repository;

import antifraud.Model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("select c from card c where c.number = ?1")
    Optional<Card> findByNumber(String number);
}
