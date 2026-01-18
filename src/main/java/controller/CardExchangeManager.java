package controller;

import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.TradeTransaction;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * Encapsulates the logic to exchange cards between collectors' binders.
 * Extracted from LiveTradeController to reduce cognitive complexity in the controller.
 */
public class CardExchangeManager {
    private static final Logger LOGGER = Logger.getLogger(CardExchangeManager.class.getName());

    private final IBinderDao binderDao;
    private final model.api.ICardProvider cardProvider;

    public CardExchangeManager(IBinderDao binderDao, model.api.ICardProvider cardProvider) {
        this.binderDao = binderDao;
        this.cardProvider = cardProvider;
    }

    public void executeExchange(TradeTransaction tx) {
        if (tx == null) return;
        transferOfferedCardsToReceiver(tx);
        transferRequestedCardsToProposer(tx);
        removeOfferedCardsFromProposer(tx);
        removeRequestedCardsFromReceiver(tx);
    }

    private void transferOfferedCardsToReceiver(TradeTransaction tx) {
        String receiver = tx.getReceiverId();
        for (Card card : tx.getOfferedCards()) {
            String setId = card.getId().split("-")[0];
            List<Binder> receiverBinders = binderDao.getUserBinders(receiver);
            Binder binder = receiverBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            if (binder == null) {
                String setName = cardProvider.getAllSets().get(setId);
                binderDao.createBinder(receiver, setId, setName);
                receiverBinders = binderDao.getUserBinders(receiver);
                binder = receiverBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            }
            if (binder != null) {
                binder.addCard(card.toBean());
                binderDao.save(binder);
            }
        }
    }

    private void transferRequestedCardsToProposer(TradeTransaction tx) {
        String proposer = tx.getProposerId();
        for (Card card : tx.getRequestedCards()) {
            String setId = card.getId().split("-")[0];
            List<Binder> proposerBinders = binderDao.getUserBinders(proposer);
            Binder binder = proposerBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            if (binder == null) {
                String setName = cardProvider.getAllSets().get(setId);
                binderDao.createBinder(proposer, setId, setName);
                proposerBinders = binderDao.getUserBinders(proposer);
                binder = proposerBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            }
            if (binder != null) {
                binder.addCard(card.toBean());
                binderDao.save(binder);
            }
        }
    }

    private void removeOfferedCardsFromProposer(TradeTransaction tx) {
        for (Card card : tx.getOfferedCards()) {
            String proposer = tx.getProposerId();
            String setId = card.getId().split("-")[0];
            List<Binder> proposerBinders = binderDao.getUserBinders(proposer);
            Binder binder = proposerBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            if (binder == null) {
                LOGGER.fine(() -> "CardExchangeManager: proposer binder not found for set=" + setId + " owner=" + proposer);
                continue;
            }
            List<model.bean.CardBean> cards = binder.getCards();
            boolean modified = false;
            for (ListIterator<model.bean.CardBean> it = cards.listIterator(); it.hasNext(); ) {
                model.bean.CardBean cb = it.next();
                if (cb != null && card.getId().equals(cb.getId())) {
                    int remaining = cb.getQuantity() - card.getQuantity();
                    if (remaining > 0) {
                        cb.setQuantity(remaining);
                    } else {
                        it.remove();
                    }
                    modified = true;
                    break;
                }
            }
            if (modified) {
                binder.setCards(cards);
                binderDao.save(binder);
            } else {
                LOGGER.fine(() -> "CardExchangeManager: could not find offered card " + card.getId() + " in proposer binder for owner=" + proposer);
            }
        }
    }

    private void removeRequestedCardsFromReceiver(TradeTransaction tx) {
        for (Card card : tx.getRequestedCards()) {
            String receiver = tx.getReceiverId();
            String setId = card.getId().split("-")[0];
            List<Binder> receiverBinders = binderDao.getUserBinders(receiver);
            Binder binder = receiverBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            if (binder == null) {
                LOGGER.fine(() -> "CardExchangeManager: receiver binder not found for set=" + setId + " owner=" + receiver);
                continue;
            }
            List<model.bean.CardBean> cards = binder.getCards();
            boolean modified = false;
            for (ListIterator<model.bean.CardBean> it = cards.listIterator(); it.hasNext(); ) {
                model.bean.CardBean cb = it.next();
                if (cb != null && card.getId().equals(cb.getId())) {
                    int remaining = cb.getQuantity() - card.getQuantity();
                    if (remaining > 0) {
                        cb.setQuantity(remaining);
                    } else {
                        it.remove();
                    }
                    modified = true;
                    break;
                }
            }
            if (modified) {
                binder.setCards(cards);
                binderDao.save(binder);
            } else {
                LOGGER.fine(() -> "CardExchangeManager: could not find requested card " + card.getId() + " in receiver binder for owner=" + receiver);
            }
        }
    }
}
