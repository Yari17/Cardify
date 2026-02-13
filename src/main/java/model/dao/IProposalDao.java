package model.dao;

import model.domain.Proposal;
import model.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per la gestione delle proposte di scambio tra utenti.
 * Permette il tracciamento delle proposte inviate, ricevute e completate.
 */
public interface IProposalDao {

    /**
     * Recupera una proposta tramite il suo ID unico.
     * 
     * @param proposalId ID della proposta.
     * @return Un Optional contenente la proposta se trovata.
     */
    Optional<Proposal> getById(String proposalId);

    /**
     * Salva una nuova proposta di scambio.
     * 
     * @param proposal La proposta da creare.
     */
    void save(Proposal proposal);

    /**
     * Aggiorna lo stato o i dettagli di una proposta esistente.
     * 
     * @param proposal La proposta con i nuovi dati.
     */
    void update(Proposal proposal);

    /**
     * Elimina una proposta dal sistema.
     * 
     * @param proposal La proposta da rimuovere.
     */
    void delete(Proposal proposal);

    /**
     * Recupera le proposte inviate dall'utente che sono ancora in attesa di
     * risposta.
     * 
     * @param user L'utente proponente.
     * @return Lista di proposte pendenti inviate.
     */
    List<Proposal> getSentPendingProposal(User user);

    /**
     * Recupera le proposte ricevute dall'utente che sono ancora in attesa di
     * risposta.
     * 
     * @param user L'utente ricevente.
     * @return Lista di proposte pendenti ricevute.
     */
    List<Proposal> getReceivedPendingProposals(User user);

    /**
     * Recupera lo storico delle proposte concluse (accettate o rifiutate) per
     * l'utente.
     * 
     * @param user L'utente coinvolto.
     * @return Lista di proposte completate.
     */
    List<Proposal> getCompletedProposals(User user);
}
