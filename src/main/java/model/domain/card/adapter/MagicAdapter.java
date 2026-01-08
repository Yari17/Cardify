package model.domain.card.adapter;

import model.bean.CardBean;
import model.bean.MagicCardBean;

import java.util.ArrayList;
import java.util.List;

public class MagicAdapter implements ICardApiAdapter<MagicCardBean> {

    public MagicAdapter() {
        // TODO: Inizializza client API Magic (es. Scryfall)
    }

    @Override
    public List<CardBean> search(String query) {
        // TODO: Implementare quando avrai l'API Magic
        // Esempio con Scryfall:
        // return scryfallClient.cards().search(query).stream()
        //     .map(this::convertToMagicCardBean)
        //     .collect(Collectors.toList());

        return new ArrayList<>(); // Stub temporaneo
    }

    @Override
    public List<CardBean> searchSet(String setID) {
        // TODO: Implementare quando avrai l'API Magic
        return new ArrayList<>(); // Stub temporaneo
    }

    @Override
    public CardBean getCardById(String id) {
        // TODO: Implementare quando avrai l'API Magic
        return null; // Stub temporaneo
    }

    @Override
    public MagicCardBean getCardDetails(String id) {
        // TODO: Implementare quando avrai l'API Magic
        return null;
    }

    private MagicCardBean convertToMagicCardBean(Object apiCard) {
        // TODO: Implementare conversione da API Magic a MagicCardBean
        return null;
    }
}
