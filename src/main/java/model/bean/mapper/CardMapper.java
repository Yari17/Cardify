package model.bean.mapper;

import model.bean.CardBean;
import model.domain.Card;
import model.domain.CollectionItem;

/**
 * Classe di utility per la conversione tra oggetti di dominio e Bean relativi
 * alle carte.
 * Facilita il distacco del livello di persistenza dalla logica di
 * presentazione.
 */
public class CardMapper {

    /**
     * Costruttore privato per nascondere quello pubblico di default (Utility
     * Class).
     */
    private CardMapper() {
    }

    /**
     * Converte un'entità {@link Card} in un {@link CardBean}.
     * Copia i dati base della carta e imposta la quantità fissa a 1.
     * 
     * @param card L'oggetto di dominio da convertire.
     * @return L'istanza di CardBean popolata o null.
     */
    public static CardBean toBean(Card card) {
        if (card == null) {
            return null;
        }
        CardBean bean = new CardBean();
        bean.setId(card.getCardID());
        bean.setName(card.getCardName());
        bean.setImageUrl(card.getCardImage());
        bean.setGameType(card.getGameType());
        bean.setSetId(card.getCardSetID());
        bean.setQuantity(1);
        bean.setDetails(card.getDetails());
        return bean;
    }

    /**
     * Converte un {@link CollectionItem} (che lega carta e quantità) in un
     * {@link CardBean}.
     * Utile per rappresentare carte possedute in collezione con la relativa
     * numerosità.
     * 
     * @param item L'elemento della collezione da convertire.
     * @return L'istanza di CardBean con quantità popolata o null.
     */
    public static CardBean toBean(CollectionItem item) {
        if (item == null || item.getCard() == null) {
            return null;
        }
        Card card = item.getCard();
        CardBean bean = new CardBean();
        bean.setId(card.getCardID());
        bean.setName(card.getCardName());
        bean.setImageUrl(card.getCardImage());
        bean.setGameType(card.getGameType());
        bean.setSetId(card.getCardSetID());
        bean.setQuantity(item.getQuantity());
        bean.setDetails(card.getDetails());
        return bean;
    }

    /**
     * Converte un {@link CardBean} nell'entità di dominio {@link Card}.
     * 
     * @param bean Il Bean contenente i dati della carta.
     * @return L'istanza di Card popolata o null.
     */
    public static Card toDomain(CardBean bean) {
        if (bean == null) {
            return null;
        }
        Card card = new Card(bean.getName(), bean.getId(), bean.getSetId(), bean.getImageUrl(), bean.getGameType());
        card.setDetails(bean.getDetails());
        return card;
    }
}
