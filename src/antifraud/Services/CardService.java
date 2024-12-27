package antifraud.Services;

import antifraud.Model.Card;
import antifraud.Repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    @Autowired

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public void newCard(String cardNumber) {
        Card card = new Card(cardNumber);
        cardRepository.save(card);
    }

    public Card getCard(String cardNumber) {
        Optional<Card> card = cardRepository.findByNumber(cardNumber);
        if (card.isPresent()) {
            return card.get();
        }
        Card newCard = new Card(cardNumber);
        return cardRepository.save(newCard);
    }


}
