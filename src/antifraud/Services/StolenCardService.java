package antifraud.Services;

import antifraud.DTO.StolenCardRequestDTO;
import antifraud.DTO.StolenCardResponseDTO;
import antifraud.Errors.AlreadyExistsException;
import antifraud.Errors.BadFormatException;
import antifraud.Errors.UserNotFoundException;
import antifraud.Model.IP;
import antifraud.Model.StolenCard;
import antifraud.Repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StolenCardService {
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public List<StolenCardResponseDTO> findAll() {
        Iterable<StolenCard> cards = stolenCardRepository.findAll();
        List<StolenCardResponseDTO> cardList = new ArrayList<>();
        for (StolenCard stolenCard : cards) {
            cardList.add(new StolenCardResponseDTO(stolenCard.getId(), stolenCard.getNumber()));
        }
        return cardList;
    }

    public StolenCard deleteByNumber(String number) {
        Optional<StolenCard> stolenCardOptional = stolenCardRepository.findByNumber(number);
        if (isInvalidCardNumber(number)) {
            throw new BadFormatException();
        }
        if (stolenCardOptional.isEmpty()) {
            throw new UserNotFoundException();
        }
        StolenCard cardToDelete = stolenCardOptional.get();
        stolenCardRepository.delete(cardToDelete);
        return cardToDelete;
    }



    public StolenCard addNewCard(StolenCardRequestDTO stolenCardRequestDTO) {
        if(stolenCardRepository.findByNumber(stolenCardRequestDTO.number()).isPresent()) {
            throw new AlreadyExistsException();
        }
        if(isInvalidCardNumber(stolenCardRequestDTO.number())) {
            throw new BadFormatException();
        }
        StolenCard stolenCard = new StolenCard(stolenCardRequestDTO.number());
        stolenCardRepository.save(stolenCard);
        return stolenCard;

    }

    public boolean isInvalidCardNumber(String number) {
        int[] array = convertToArray(number);
        int lastDigit = array[array.length - 1]; //first step is to remove check digit (last)
        array[array.length - 1] = 0;

        doubleEverySecondNumber(array); //every second number is doubled (it goes right to left but this is the same)
        reduceOverNineNumbersByNine(array); //if a number is over 9 then we take 9 off it

        int total = sumCardNumber(array); //get the sum of the numbers we have after those operations

        return (total + lastDigit) % 10 !=0;

    }

    private int[] convertToArray(String number) {
        int[] convertedArray = new int[number.length()];
        for (int i = 0; i < number.length(); i++) {
            convertedArray[i] = number.charAt(i) - 48;
        }
        return convertedArray;
    }
    private void reduceOverNineNumbersByNine(int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] > 9) {
                array[i] -= 9;
            }
        }
    }
    private void doubleEverySecondNumber(int[] array) {
        for (int i = 0; i < array.length; i++) {
            if ((i+1) % 2 != 0) {
                array[i] *= 2;
            }
        }
    }
    private int sumCardNumber(int[] array) {
        int sum = 0;
        for (int i: array) {
            sum += i;
        }
        return sum;
    }

}

