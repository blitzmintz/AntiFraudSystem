package antifraud.Controllers;

import antifraud.DTO.*;
import antifraud.Errors.UserNotFoundException;
import antifraud.Model.IP;
import antifraud.Model.StolenCard;
import antifraud.Model.Transaction;
import antifraud.Services.IPService;
import antifraud.Services.StolenCardService;
import antifraud.Services.TransactionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;
    private final IPService ipService;
    private final StolenCardService stolenCardService;

    public TransactionController(TransactionService transactionService, IPService ipService, StolenCardService stolenCardService) {
        this.transactionService = transactionService;
        this.ipService = ipService;
        this.stolenCardService = stolenCardService;
    }
    //TRANSACTIONS BELOW

    @PostMapping(value = {"api/antifraud/transaction"})
    @ResponseBody
    public ResponseEntity<ResultDTO> postTransaction(@RequestBody @Valid TransactionDTO transactionDTO) {
        return transactionService.validateTransaction(transactionDTO);

    }
    //SUSPICIOUS IP BELOW

    @PostMapping(value = "api/antifraud/suspicious-ip")
    @ResponseBody
    public ResponseEntity<SuspiciousIPResponseDTO> postSuspiciousIP(@RequestBody SuspiciousIPRequestDTO suspiciousIPRequestDTO) {
        IP ip = ipService.addSuspiciousIp(suspiciousIPRequestDTO);
        SuspiciousIPResponseDTO response = new SuspiciousIPResponseDTO(ip.getId(), ip.getIp());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "api/antifraud/suspicious-ip")
    @ResponseBody
    public ResponseEntity<List<SuspiciousIPResponseDTO>> getSuspiciousIP() {
        List<SuspiciousIPResponseDTO> ips = ipService.getAllIp();
        return new ResponseEntity<>(ips, HttpStatus.OK);
    }

    @DeleteMapping(value = "api/antifraud/suspicious-ip/{ip}")
    @ResponseBody
    public ResponseEntity<DeleteDTO> deleteSuspiciousIP(@PathVariable String ip) {
        IP ipDelete = ipService.deleteByIp(ip);
        String ipAddress = ipDelete.getIp();
        return new ResponseEntity<>(new DeleteDTO(String.format("IP %s successfully removed!", ipAddress)), HttpStatus.OK);

    }
    //STOLEN CARD BELOW

    @GetMapping(value = "api/antifraud/stolencard")
    @ResponseBody
    public ResponseEntity<List<StolenCardResponseDTO>> getStolenCard() {
        List<StolenCardResponseDTO> cards = stolenCardService.findAll();
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @PostMapping(value = "api/antifraud/stolencard")
    @ResponseBody
    public ResponseEntity<StolenCardResponseDTO> postStolenCard(@RequestBody StolenCardRequestDTO stolenCardRequestDTO) {
        StolenCard stolenCard = stolenCardService.addNewCard(stolenCardRequestDTO);
        StolenCardResponseDTO response = new StolenCardResponseDTO(stolenCard.getId(), stolenCard.getNumber());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "api/antifraud/stolencard/{number}")
    @ResponseBody
    public ResponseEntity<DeleteDTO> deleteStolenCard(@PathVariable String number) {
        StolenCard stolenCardDelete = stolenCardService.deleteByNumber(number);
        String stolenCardNumber = stolenCardDelete.getNumber();
        return new ResponseEntity<>(new DeleteDTO(String.format("Card %s successfully removed!", stolenCardNumber)), HttpStatus.OK);
    }

    @PutMapping(value = "api/antifraud/transaction")
    @ResponseBody
    public ResponseEntity<TransactionDTO> putTransactionFeedback(@RequestBody @Valid TransactionFeedbackDTO transactionFeedbackDTO) {
        Transaction transaction = transactionService.updateTransactionFeedback(transactionFeedbackDTO);
        return new ResponseEntity<>(new TransactionDTO(transaction.getId(), transaction.getAmount(), transaction.getIp(), transaction.getNumber(), transaction.getRegion(), transaction.getDate(), transaction.getStatus(), transaction.getFeedback()), HttpStatus.OK);
    } //need to add to security config

    @GetMapping(value = "api/antifraud/history")
    @ResponseBody
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);

    } //need to add to security config

    @GetMapping(value = "api/antifraud/history/{number}")
    @ResponseBody
    public ResponseEntity<List<TransactionDTO>> getTransactionHistoryForCard(@PathVariable String number) {
        List<TransactionDTO> transactions = transactionService.getTransactionForCard(number);
        if (transactions.isEmpty()) {
            throw new UserNotFoundException();
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    } //need to add to security config




}
