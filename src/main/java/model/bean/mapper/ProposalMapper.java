package model.bean.mapper;

import model.bean.CardBean;
import model.bean.ProposalBean;
import model.domain.CollectionItem;
import model.domain.Proposal;
import model.domain.User;
import model.domain.enumerations.ProposalStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utility per la conversione tra l'entità {@link Proposal} e
 * {@link ProposalBean}.
 * Si occupa della formattazione delle date e della trasformazione delle liste
 * di item in bean.
 */
public class ProposalMapper {

    /** Formattatore per la data (giorno/mese/anno). */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Formattatore per l'ora (ore:minuti). */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Costruttore privato per prevenire l'istanziazione.
     */
    private ProposalMapper() {
    }

    /**
     * Map una {@link Proposal} di dominio in un {@link ProposalBean}.
     * Formatta i timestamp per la visualizzazione testuale e delega il mapping
     * delle carte
     * a {@link CardMapper}.
     * 
     * @param proposal La proposta di dominio da mappare.
     * @return Il bean popolato o null.
     */
    public static ProposalBean toBean(Proposal proposal) {
        if (proposal == null) {
            return null;
        }
        ProposalBean bean = new ProposalBean();
        bean.setProposalId(proposal.getId());

        if (proposal.getProposer() != null) {
            bean.setFromUser(proposal.getProposer().getUsername());
        }
        if (proposal.getReceiver() != null) {
            bean.setToUser(proposal.getReceiver().getUsername());
        }

        if (proposal.getStatus() != null) {
            bean.setStatus(proposal.getStatus().toString());
        }

        if (proposal.getMeetingStore() != null) {
            bean.setMeetingPlace(proposal.getMeetingStore().getUsername());
        }

        LocalDateTime scheduled = proposal.getScheduledAt();
        if (scheduled != null) {
            // Controlla se l'uso del bean esistente prevede Data/Ora separati
            bean.setMeetingDate(scheduled.format(DATE_FORMATTER));
            bean.setMeetingTime(scheduled.format(TIME_FORMATTER));
        }

        if (proposal.getProposedItems() != null) {
            List<CardBean> offered = new ArrayList<>();
            for (CollectionItem item : proposal.getProposedItems()) {
                offered.add(CardMapper.toBean(item));
            }
            bean.setOffered(offered);
        }

        if (proposal.getAskedItems() != null) {
            List<CardBean> requested = new ArrayList<>();
            for (CollectionItem item : proposal.getAskedItems()) {
                requested.add(CardMapper.toBean(item));
            }
            bean.setRequested(requested);
        }

        return bean;
    }

    /**
     * Converte un {@link ProposalBean} in un oggetto di dominio {@link Proposal}.
     * Ricostruisce gli oggetti User e CollectionItem dai dati del bean.
     * 
     * @param bean Il bean da convertire.
     * @return L'oggetto di dominio popolato o null.
     */
    public static Proposal toDomain(ProposalBean bean) {
        if (bean == null) {
            return null;
        }

        // Ricostruzione di utenti minimi (solo username)
        User proposer = new User(bean.getFromUser(), null, null);
        User receiver = new User(bean.getToUser(), null, null);
        User store = null;
        if (bean.getMeetingPlace() != null) {
            store = new User(bean.getMeetingPlace(), null, null);
        }

        List<CollectionItem> proposedItems = new ArrayList<>();
        if (bean.getOffered() != null) {
            for (CardBean cb : bean.getOffered()) {
                // CardMapper.toDomain restituisce Card. CollectionItem necessita di Carta +
                // quantità
                proposedItems.add(new CollectionItem(CardMapper.toDomain(cb), cb.getQuantity()));
            }
        }

        List<CollectionItem> askedItems = new ArrayList<>();
        if (bean.getRequested() != null) {
            for (CardBean cb : bean.getRequested()) {
                askedItems.add(new CollectionItem(CardMapper.toDomain(cb), cb.getQuantity()));
            }
        }

        Proposal proposal = new Proposal(bean.getProposalId(), proposer, receiver, proposedItems, askedItems, store);

        if (bean.getStatus() != null) {
            try {
                proposal.setStatus(ProposalStatus.valueOf(bean.getStatus()));
            } catch (Exception _) {
                // ignora o logga
            }
        }

        // Ricostruzione della data programmata
        if (bean.getMeetingDate() != null && bean.getMeetingTime() != null) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(bean.getMeetingDate(), DATE_FORMATTER);
                java.time.LocalTime time = java.time.LocalTime.parse(bean.getMeetingTime(), TIME_FORMATTER);
                proposal.setScheduledAt(LocalDateTime.of(date, time));
            } catch (Exception _) {
                // ignora errore di parsing
            }
        }

        return proposal;
    }
}
