package model.bean.mapper;

import model.bean.CardBean;
import model.bean.TradeSessionBean;
import model.domain.Card;
import model.domain.TradeSession;
import model.domain.enumerations.TradeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utility per la conversione tra l'entità {@link TradeSession} e
 * {@link TradeSessionBean}.
 * Gestisce l'appiattimento delle strutture complesse (Participants, Details)
 * nel bean
 * e viceversa, mantenendo la coerenza degli stati di arrivo e ispezione.
 */
public class TradeSessionMapper {

    /**
     * Costruttore privato per prevenire l'istanziazione.
     */
    private TradeSessionMapper() {
    }

    /**
     * Converte una {@link TradeSession} di dominio nel relativo
     * {@link TradeSessionBean}.
     * Estrae i dati dai raggruppamenti interni del dominio per popolare i campi
     * piatti del bean.
     * 
     * @param session La sessione di dominio da convertire.
     * @return Il bean popolato o null.
     */
    public static TradeSessionBean toBean(TradeSession session) {
        if (session == null) {
            return null;
        }
        TradeSessionBean bean = new TradeSessionBean();
        bean.setTransactionId(session.getSessionId());
        bean.setProposerId(session.getProposerId());
        bean.setReceiverId(session.getReceiverId());
        bean.setStoreId(session.getStoreId());
        bean.setTradeDate(session.getTradeDate());

        if (session.getTradeStatus() != null) {
            bean.setStatus(session.getTradeStatus().toString());
        }

        bean.setProposerSessionCode(session.getProposerSessionCode());
        bean.setReceiverSessionCode(session.getReceiverSessionCode());

        bean.setProposerArrived(session.isProposerArrived());
        bean.setReceiverArrived(session.isReceiverArrived());

        bean.setProposerInspectionOk(session.getProposerInspectionOk());
        bean.setReceiverInspectionOk(session.getReceiverInspectionOk());

        if (session.getOfferedCards() != null) {
            List<CardBean> offered = new ArrayList<>();
            for (Card c : session.getOfferedCards()) {
                offered.add(CardMapper.toBean(c));
            }
            bean.setOffered(offered);
        }

        if (session.getRequestedCards() != null) {
            List<CardBean> requested = new ArrayList<>();
            for (Card c : session.getRequestedCards()) {
                requested.add(CardMapper.toBean(c));
            }
            bean.setRequested(requested);
        }

        return bean;
    }

    /**
     * Mappa una lista di TradeSession in una lista di TradeSessionBean.
     * Delega la responsabilità di mapping al mapper (Information Expert),
     * evitando duplicazione nei controller (High Cohesion).
     *
     * @param sessions lista di TradeSession da mappare
     * @return lista di TradeSessionBean mappati, lista vuota se input null
     */
    public static List<TradeSessionBean> toBeanList(List<TradeSession> sessions) {
        List<TradeSessionBean> beans = new ArrayList<>();
        if (sessions == null) {
            return beans;
        }
        for (TradeSession session : sessions) {
            beans.add(toBean(session));
        }
        return beans;
    }

    /**
     * Converte un {@link TradeSessionBean} in un oggetto di dominio
     * {@link TradeSession}.
     * Ricostruisce la struttura complessa a partire dai dati piatti del bean.
     * 
     * @param bean Il bean da convertire.
     * @return L'oggetto di dominio popolato o null.
     */
    public static TradeSession toDomain(TradeSessionBean bean) {
        if (bean == null) {
            return null;
        }

        String pId = bean.getProposerId();
        String rId = bean.getReceiverId();
        String sId = bean.getStoreId();

        TradeSession.TradeParticipants participants = new TradeSession.TradeParticipants(pId, rId, sId);

        List<Card> offered = new ArrayList<>();
        if (bean.getOffered() != null) {
            for (CardBean cb : bean.getOffered()) {
                offered.add(CardMapper.toDomain(cb));
            }
        }

        List<Card> requested = new ArrayList<>();
        if (bean.getRequested() != null) {
            for (CardBean cb : bean.getRequested()) {
                requested.add(CardMapper.toDomain(cb));
            }
        }

        TradeSession.TradeDetails details = new TradeSession.TradeDetails(
                null, // Il timestamp di creazione potrebbe essere perso o richiedere un altro campo
                      // nel bean se critico
                bean.getTradeDate(),
                offered,
                requested);

        TradeStatus status = TradeStatus.WAITING_FOR_ARRIVAL; // Predefinito
        if (bean.getStatus() != null) {
            try {
                status = TradeStatus.valueOf(bean.getStatus());
            } catch (Exception _) {
                // ignora
            }
        }

        TradeSession session = new TradeSession(bean.getTransactionId(), status, participants, details);

        session.setProposerSessionCode(bean.getProposerSessionCode());
        session.setReceiverSessionCode(bean.getReceiverSessionCode());
        session.setProposerArrived(bean.isProposerArrived());
        session.setReceiverArrived(bean.isReceiverArrived());

        if (bean.getProposerInspectionOk() != null) {
            session.markInspectionResult(pId, bean.getProposerInspectionOk());
        }
        if (bean.getReceiverInspectionOk() != null) {
            session.markInspectionResult(rId, bean.getReceiverInspectionOk());
        }

        return session;
    }
}
