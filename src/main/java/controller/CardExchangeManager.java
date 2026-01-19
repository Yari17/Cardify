package controller;

import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.TradeTransaction;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;


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
        removeCardsFromOwner(tx.getProposerId(), tx.getOfferedCards(), this::logProposerBinderMissing, this::logProposerCardMissing);
    }

    private void removeRequestedCardsFromReceiver(TradeTransaction tx) {
        removeCardsFromOwner(tx.getReceiverId(), tx.getRequestedCards(), this::logReceiverBinderMissing, this::logReceiverCardMissing);
    }

    
    @FunctionalInterface
    private interface BinderMissingLogger {
        void log(String setId, String owner);
    }

    @FunctionalInterface
    private interface CardMissingLogger {
        void log(String cardId, String owner);
    }

    
    private void removeCardsFromOwner(String owner, List<Card> cardsToRemove, BinderMissingLogger missingBinderLog,
                                      CardMissingLogger missingCardLog) {
        if (owner == null || cardsToRemove == null) return;
        for (Card card : cardsToRemove) {
            String setId = card.getId().split("-")[0];
            List<Binder> ownerBinders = binderDao.getUserBinders(owner);
            Binder binder = ownerBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            if (binder == null) {
                missingBinderLog.log(setId, owner);
                continue;
            }
            boolean removed = removeCardFromBinder(binder, card);
            if (!removed) {
                missingCardLog.log(card.getId(), owner);
            }
        }
    }

    
    
    private boolean removeCardFromBinder(Binder binder, Card card) {
        if (binder == null || card == null) return false;
        List<model.bean.CardBean> cards = binder.getCards();
        for (ListIterator<model.bean.CardBean> it = cards.listIterator(); it.hasNext(); ) {
            model.bean.CardBean cb = it.next();
            if (cb != null && card.getId().equals(cb.getId())) {
                int remaining = cb.getQuantity() - card.getQuantity();
                if (remaining > 0) {
                    cb.setQuantity(remaining);
                } else {
                    it.remove();
                }
                binder.setCards(cards);
                binderDao.save(binder);
                return true;
            }
        }
        return false;
    }

    
    private void logProposerBinderMissing(String setId, String owner) {
        LOGGER.fine(() -> "CardExchangeManager: proposer binder not found for set=" + setId + " owner=" + owner);
    }

    private void logProposerCardMissing(String cardId, String owner) {
        LOGGER.fine(() -> "CardExchangeManager: could not find offered card " + cardId + " in proposer binder for owner=" + owner);
    }

    private void logReceiverBinderMissing(String setId, String owner) {
        LOGGER.fine(() -> "CardExchangeManager: receiver binder not found for set=" + setId + " owner=" + owner);
    }

    private void logReceiverCardMissing(String cardId, String owner) {
        LOGGER.fine(() -> "CardExchangeManager: could not find requested card " + cardId + " in receiver binder for owner=" + owner);
    }

}
