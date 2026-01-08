package model.domain.card.adapter;

import model.bean.CardBean;
import model.bean.PokemonCardBean;
import model.domain.CardGameType;
import net.tcgdex.sdk.TCGdex;
import net.tcgdex.sdk.models.Card;
import net.tcgdex.sdk.models.CardResume;
import net.tcgdex.sdk.models.Set;

import java.util.ArrayList;
import java.util.List;

public class PokemonAdapter implements ICardApiAdapter<PokemonCardBean> {
    private final TCGdex api;

    public PokemonAdapter() {
        this.api = new TCGdex("en");
    }

    @Override
    public List<CardBean> search(String query) {
        return new ArrayList<>();
    }

    @Override
    public List<CardBean> searchSet(String setID) {
        try {
            Set set = api.fetchSet(setID);
            List<CardResume> cards = set.getCards();

            List<CardBean> cardBeans = new ArrayList<>();
            for (CardResume cardResume : cards) {
                String imageUrl = cardResume.getImage() != null ?
                    cardResume.getImage() + "/high.png" : null;

                CardBean cardBean = new CardBean(
                    cardResume.getId(),
                    cardResume.getName(),
                    imageUrl,
                    CardGameType.POKEMON
                );
                cardBeans.add(cardBean);
            }

            return cardBeans;
        } catch (NullPointerException _) {
            return new ArrayList<>();
        }
    }

    @Override
    public CardBean getCardById(String id) {
        return null;
    }

    @Override
    public PokemonCardBean getCardDetails(String id) {
        return null;
    }

    private PokemonCardBean convertToPokemonCardBean(Card apiCard) {
        return null;
    }
}
