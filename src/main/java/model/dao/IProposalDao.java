package model.dao;

import model.domain.Proposal;

import java.util.List;
import java.util.Optional;

public interface IProposalDao {
    List<Proposal> getAll();

    Optional<Proposal> getById(String proposalId);

    void save(Proposal proposal);

    void update(Proposal proposal);

    void delete(Proposal proposal);

    List<Proposal> getSentPendingProposal(String username);
    List<Proposal> getReceivedProposals(String username);
    List<Proposal> getScheduledProposals(String username);
}
