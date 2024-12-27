package antifraud.Services;

import antifraud.DTO.ResultDTO;
import antifraud.DTO.TransactionDTO;
import antifraud.DTO.TransactionFeedbackDTO;
import antifraud.Enums.Region;
import antifraud.Enums.TransactionResponse;
import antifraud.Errors.AlreadyExistsException;
import antifraud.Errors.BadFormatException;
import antifraud.Errors.UnprocessableEntityException;
import antifraud.Errors.UserNotFoundException;
import antifraud.Model.Card;
import antifraud.Model.IP;
import antifraud.Model.Transaction;
import antifraud.Repository.CardRepository;
import antifraud.Repository.IPRepository;
import antifraud.Repository.StolenCardRepository;
import antifraud.Repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
public class TransactionService {

    private final Integer MAX_AMOUNT_FOR_ALLOWED = 200;
    private final Integer MAX_AMOUNT_FOR_MANUAL_PROCESSING = 1500;

    private final StolenCardService stolenCardService;
    private final IPService ipService;
    private final StolenCardRepository stolenCardRepository;
    private final IPRepository ipRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;


    public TransactionService(StolenCardService stolenCardService, IPService ipService, StolenCardRepository stolenCardRepository, IPRepository ipRepository, TransactionRepository transactionRepository, CardRepository cardRepository, CardService cardService) {
        this.stolenCardService = stolenCardService;
        this.ipService = ipService;
        this.stolenCardRepository = stolenCardRepository;
        this.ipRepository = ipRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }
    private HttpStatus status;
    private TransactionResponse transactionResponse;
    private List<String> info;

    public Transaction updateTransactionFeedback(TransactionFeedbackDTO transactionFeedbackDTO) {
        Optional<Transaction> optTransaction = transactionRepository.findById(transactionFeedbackDTO.transactionId());
        if (optTransaction.isEmpty()) {
            throw new UserNotFoundException();
        }
        Transaction transaction = optTransaction.get();
        Card card = cardService.getCard(transaction.getNumber());
        TransactionResponse transactionFeedback;

        try {
            transactionFeedback = TransactionResponse.valueOf(transactionFeedbackDTO.feedback());
        } catch (IllegalArgumentException e) {
            throw new BadFormatException();
        }

        if (transaction.getFeedback() != null) {
            throw new AlreadyExistsException();
        }



        if (transaction.getStatus().toString().equals(transactionFeedbackDTO.feedback())) {
            throw new UnprocessableEntityException();
        }
/*
        if (Objects.equals(transactionFeedbackDTO.feedback(), TransactionResponse.ALLOWED.toString()) ||
                Objects.equals(transactionFeedbackDTO.feedback(), TransactionResponse.MANUAL_PROCESSING.toString()) ||
                Objects.equals(transactionFeedbackDTO.feedback(), TransactionResponse.PROHIBITED.toString())) {
            throw new BadFormatException();
        }
*/

        long increasedAllowed = (long) Math.ceil(0.8 * card.getMaxAllowed() + 0.2 * transaction.getAmount());
        long decreasedAllowed = (long) Math.ceil(0.8 * card.getMaxAllowed() - 0.2 * transaction.getAmount());
        long increasedProhibited = (long) Math.ceil(0.8 * card.getMaxManual() + 0.2 * transaction.getAmount());
        long decreasedProhibited = (long) Math.ceil(0.8 * card.getMaxManual() - 0.2 * transaction.getAmount());


        TransactionResponse result = TransactionResponse.valueOf(String.valueOf(transaction.getStatus()));
        transaction.setFeedback(transactionFeedback);

        switch (transactionFeedback) {
            case ALLOWED -> {
                switch (result) {
                    case MANUAL_PROCESSING -> card.setMaxAllowed(increasedAllowed);
                    case PROHIBITED -> {
                        card.setMaxAllowed(increasedAllowed);
                        card.setMaxManual(increasedProhibited);
                    }
                }
            }
            case MANUAL_PROCESSING -> {
                switch (result) {
                    case ALLOWED -> card.setMaxAllowed(decreasedAllowed);
                    case PROHIBITED -> card.setMaxManual(increasedProhibited);
                }
            }
            case PROHIBITED -> {
                switch (result) {
                    case ALLOWED -> {
                        card.setMaxManual(decreasedProhibited);
                        card.setMaxAllowed(decreasedAllowed);
                    }
                    case MANUAL_PROCESSING -> card.setMaxManual(decreasedProhibited);
                }
            }
        }
        cardRepository.save(card);

        return transactionRepository.save(transaction);

    }

    public List<TransactionDTO> getAllTransactions() {
        Iterable<Transaction> transactions = transactionRepository.findByIdNotNullOrderById();
        List<TransactionDTO> transactionList = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionList.add(new TransactionDTO(transaction.getId(), transaction.getAmount(), transaction.getIp(), transaction.getNumber(), transaction.getRegion(), transaction.getDate(), transaction.getStatus(), transaction.getFeedback()));
        }
        return transactionList;
    }

    public List<TransactionDTO> getTransactionForCard(String number) {
        if (stolenCardService.isInvalidCardNumber(number)) {
            throw new BadFormatException();
        }
        Iterable<Transaction> transactions = transactionRepository.findByNumber(number);
        List<TransactionDTO> transactionListForCard = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionListForCard.add(new TransactionDTO(transaction.getId(), transaction.getAmount(), transaction.getIp(), transaction.getNumber(), transaction.getRegion(), transaction.getDate(), transaction.getStatus(), transaction.getFeedback()));}

        return transactionListForCard;
    }



    public ResponseEntity<ResultDTO> validateTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction(transactionDTO.amount(), transactionDTO.ip(), transactionDTO.number(), transactionDTO.region(), transactionDTO.date(), null, null);
        transactionRepository.save(transaction);

        Card card = cardService.getCard(transactionDTO.number());

        Long maxAllowed = card.getMaxAllowed(); //starts as 200 for new cards
        Long maxManual = card.getMaxManual(); //starts as 1500 for new cards

        status = HttpStatus.OK;
        transactionResponse = TransactionResponse.ALLOWED;
        info = new ArrayList<>();

        List<Transaction> lastHourTransactions = transactionRepository.findByNumberAndDateBetween(
                transactionDTO.number(), transactionDTO.date().minusHours(1), transactionDTO.date());

        validIPCount(lastHourTransactions);
        validRegionCount(lastHourTransactions);
        invalidCardError(transactionDTO.number());
        invalidIpError(transactionDTO.ip());
        validAmount(transactionDTO.amount(), maxAllowed, maxManual);

        transaction.setStatus(transactionResponse);
        transactionRepository.save(transaction);


        if (status == HttpStatus.BAD_REQUEST){
            transactionRepository.deleteById(transaction.getId());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new ResultDTO(transactionResponse, infoListToString()), status);
        }
    }

    private void validRegionCount(List<Transaction> list) {
        Set<Region> regionSet = new HashSet<>();

        list.forEach(transaction -> {
            regionSet.add(transaction.getRegion());
        });

        int regionCount = regionSet.size();
        if ((regionCount - 1) >= 3) {
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("region-correlation");
        } else if ((regionCount - 1) == 2) {
            transactionResponse = TransactionResponse.MANUAL_PROCESSING;
            info.add("region-correlation");
        }

    }

    private void validIPCount(List<Transaction> list) {
        Set<String> ipSet = new HashSet<>();

        list.forEach(transaction -> {
            ipSet.add(transaction.getIp());
        });

        int ipCount = ipSet.size();
        if((ipCount - 1) >= 3) {
            transactionResponse = TransactionResponse.PROHIBITED;

            info.add("ip-correlation");
        } else if ((ipCount - 1) == 2) {
            transactionResponse = TransactionResponse.MANUAL_PROCESSING;
            info.add("ip-correlation");
        }
    }

    private void validAmount(Long amount, Long maxAllowed, Long maxManual) {
        if (amount == null || amount <= 0) {
            status = HttpStatus.BAD_REQUEST;
        } else if (amount > maxManual) {
            status = HttpStatus.OK;
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("amount");
        } else if (amount > maxAllowed && info.isEmpty()) {
            status = HttpStatus.OK;
            transactionResponse = TransactionResponse.MANUAL_PROCESSING;
            info.add("amount");
        } else if (amount <= maxAllowed && info.isEmpty()) {
            transactionResponse = TransactionResponse.ALLOWED;
        }
    }

    private void invalidCardError(String number) {
        if(stolenCardService.isInvalidCardNumber(number)) {
            status = HttpStatus.BAD_REQUEST;
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("card-number");
        } else if (stolenCardRepository.findByNumber(number).isPresent()) {
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("card-number");
        }
    }

    private void invalidIpError(String ip) {
        if(ipService.isInvalidIp(ip)) {
            status = HttpStatus.BAD_REQUEST;
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("ip");
        } else if(ipRepository.findByIp(ip).isPresent()) {
            transactionResponse = TransactionResponse.PROHIBITED;
            info.add("ip");
        }

    }

    private String infoListToString() {
        StringBuilder infoString = new StringBuilder();
        if (info.isEmpty()) {
            info.add("none");
        }
        info.sort((String::compareToIgnoreCase));
        infoString.append(info.get(0));
        for (int i = 1; i < info.size(); i++) {
            infoString.append(", ").append(info.get(i));
        }
        return infoString.toString();
    }



}
