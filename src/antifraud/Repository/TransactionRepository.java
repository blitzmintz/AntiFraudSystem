package antifraud.Repository;

import antifraud.Model.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findById(long id);
    @Query("select distinct t from Transaction t where t.number = ?1 and t.date between ?2 and ?3")
    List<Transaction> findByNumberAndDateBetween(@NotNull String number, @NotNull LocalDateTime dateStart, @NotNull LocalDateTime dateEnd);
    @Query("select t from Transaction t where t.number = ?1")
    List<Transaction> findByNumber(@NotNull String number);
    @Query("select t from Transaction t where t.id is not null")
    List<Transaction> findByIdNotNullOrderById();
}
