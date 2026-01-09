package model.domain.card.adapter;

import model.domain.card.Card;
import model.domain.card.PokemonCard;
import model.domain.CardGameType;
import net.tcgdex.sdk.TCGdex;
import net.tcgdex.sdk.models.CardResume;
import net.tcgdex.sdk.models.Set;
import net.tcgdex.sdk.models.SetResume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokemonAdapter implements ICardApiAdapter<PokemonCard> {
    private final TCGdex api;

    public PokemonAdapter() {
        this.api = new TCGdex("en");
    }

    @Override
    public List<Card> search(String query) {
        return new ArrayList<>();
    }

    @Override
    public List<Card> searchSet(String setID) {
        try {
            Set set = api.fetchSet(setID);
            assert set != null;
            List<CardResume> cards = set.getCards();

            List<Card> cardList = new ArrayList<>();
            for (CardResume cardResume : cards) {
                String imageUrl = cardResume.getImage() != null ?
                        cardResume.getImage() + "/high.png" : null;

                Card card = new Card(
                        cardResume.getId(),
                        cardResume.getName(),
                        imageUrl,
                        CardGameType.POKEMON
                );
                cardList.add(card);
            }

            return cardList;
        } catch (NullPointerException _) {
            return new ArrayList<>();
        }
    }

    @Override
    public Set getSetDetails(String setID) {
        return api.fetchSet(setID);
    }

    @Override
    public Card getCardById(String id) {
        return null;
    }

    @Override
    public PokemonCard getCardDetails(String id) {
        return null;
    }

    @Override
    public Map<String, String> getAllSets() {
        SetResume[] setArray = api.fetchSets();
        Map<String, String> setMap = new HashMap<>();

        for (SetResume setResume : setArray) {
            setMap.put(setResume.getId(), setResume.getName());
        }
        return setMap;
    }
}
