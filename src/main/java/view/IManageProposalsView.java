package view;

import java.util.List;

import model.bean.ProposalBean;

/**
 * Interfaccia per la gestione e visualizzazione delle proposte di scambio
 * (Inviate, Ricevute, Completate).
 */
public interface IManageProposalsView extends IView {
    /**
     * Mostra l'elenco delle proposte inviate dall'utente ancora in attesa di
     * risposta.
     */
    void showSentPendingProposals(List<ProposalBean> proposals);

    /**
     * Mostra l'elenco delle proposte ricevute dall'utente e in attesa di
     * valutazione.
     */
    void showReceivedPendingProposals(List<ProposalBean> proposals);

    /** Mostra lo storico delle proposte concluse con successo. */
    void showCompletedProposals(List<ProposalBean> proposals);

    /** Mostra un dialogo di conferma dopo l'accettazione di una proposta. */
    void showProposalAcceptedDialog(Runnable onNavigate);
}
