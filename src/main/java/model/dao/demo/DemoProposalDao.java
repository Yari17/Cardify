package model.dao.demo;

import model.dao.IProposalDao;
import model.domain.Proposal;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DemoProposalDao implements IProposalDao {
    private final Map<String, Proposal> map = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0);

    @Override
    public List<Proposal> getAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public Optional<Proposal> getById(String proposalId) {
        return Optional.ofNullable(map.get(proposalId));
    }

    @Override
    public void save(Proposal proposal) {
        if (proposal.getProposalId() == null || proposal.getProposalId().isEmpty()) {
            proposal.setProposalId(String.valueOf(idGen.incrementAndGet()));
        }
        proposal.setLastUpdated(LocalDateTime.now());
        map.put(proposal.getProposalId(), proposal);
    }

    @Override
    public void update(Proposal proposal) {
        proposal.setLastUpdated(LocalDateTime.now());
        map.put(proposal.getProposalId(), proposal);
    }

    @Override
    public void delete(Proposal proposal) {
        map.remove(proposal.getProposalId());
    }

    @Override
    public List<Proposal> getSentPendingProposal(String username) {
        return List.of();
    }

    @Override
    public List<Proposal> getReceivedProposals(String username) {
        return List.of();
    }

    @Override
    public List<Proposal> getScheduledProposals(String username) {
        return List.of();
    }
}
