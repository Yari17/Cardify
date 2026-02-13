package model.dao.demo;

import model.dao.IProposalDao;
import model.domain.Proposal;
import model.domain.User;
import model.domain.enumerations.ProposalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementazione in-memory del DAO per le proposte di scambio (modalità Demo).
 * Simula il database delle proposte, gestendo ID incrementali e filtri per
 * stato.
 */
public class DemoProposalDao implements IProposalDao {

    /** Storage locale per le proposte attive e storiche. */
    private final List<Proposal> proposals = new ArrayList<>();

    /** Generatore atomico per simulare l'auto-increment del database. */
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Optional<Proposal> getById(String proposalId) {
        return proposals.stream()
                .filter(p -> p.getId().equals(proposalId))
                .findFirst();
    }

    @Override
    public void save(Proposal proposal) {
        // Se non ha ID, genera uno nuovo
        if (proposal.getId() == null || proposal.getId().isEmpty()) {
            String newId = "DEMO-PROP-" + idGenerator.getAndIncrement();
            // Crea nuova proposta con ID (Proposal è immutabile per l'ID)
            Proposal withId = new Proposal(
                    newId,
                    proposal.getProposer(),
                    proposal.getReceiver(),
                    proposal.getProposedItems(),
                    proposal.getAskedItems(),
                    proposal.getMeetingStore());
            withId.setStatus(proposal.getStatus());
            withId.setScheduledAt(proposal.getScheduledAt());
            proposals.add(withId);
        } else {
            proposals.add(proposal);
        }
    }

    @Override
    public void update(Proposal proposal) {
        // Rimuovi vecchia versione e aggiungi aggiornata
        proposals.removeIf(p -> p.getId().equals(proposal.getId()));
        proposals.add(proposal);
    }

    @Override
    public void delete(Proposal proposal) {
        proposals.removeIf(p -> p.getId().equals(proposal.getId()));
    }

    @Override
    public List<Proposal> getSentPendingProposal(User user) {
        return proposals.stream()
                .filter(p -> p.getProposer().getUsername().equals(user.getUsername()))
                .filter(p -> p.getStatus() == ProposalStatus.PENDING)
                .toList();
    }

    @Override
    public List<Proposal> getReceivedPendingProposals(User user) {
        return proposals.stream()
                .filter(p -> p.getReceiver().getUsername().equals(user.getUsername()))
                .filter(p -> p.getStatus() == ProposalStatus.PENDING)
                .toList();
    }

    @Override
    public List<Proposal> getCompletedProposals(User user) {
        return proposals.stream()
                .filter(p -> p.getProposer().getUsername().equals(user.getUsername())
                        || p.getReceiver().getUsername().equals(user.getUsername()))
                .filter(p -> p.getStatus() == ProposalStatus.ACCEPTED
                        || p.getStatus() == ProposalStatus.REJECTED)
                .toList();
    }
}
