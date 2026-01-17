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
        if (proposal == null || proposal.getProposalId() == null) return;
        proposal.setLastUpdated(LocalDateTime.now());
        map.put(proposal.getProposalId(), proposal);
    }

    @Override
    public void delete(Proposal proposal) {
        if (proposal == null || proposal.getProposalId() == null) return;
        map.remove(proposal.getProposalId());
    }

    @Override
    public List<Proposal> getSentPendingProposal(String username) {
        List<Proposal> result = new ArrayList<>();
        if (username == null) return result;
        for (Proposal p : map.values()) {
            if (p == null) continue;
            if (username.equals(p.getProposerId())) {
                var s = p.getStatus();
                if (s == model.domain.enumerations.ProposalStatus.PENDING
                        || s == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getReceivedProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        if (username == null) return result;
        for (Proposal p : map.values()) {
            if (p == null) continue;
            if (username.equals(p.getReceiverId())) {
                var s = p.getStatus();
                if (s == model.domain.enumerations.ProposalStatus.PENDING
                        || s == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getScheduledProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        if (username == null) return result;
        for (Proposal p : map.values()) {
            if (p == null) continue;
            boolean involves = username.equals(p.getProposerId()) || username.equals(p.getReceiverId());
            if (involves && p.getStatus() == model.domain.enumerations.ProposalStatus.ACCEPTED) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getPendingProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : map.values()) {
            if (p == null) continue;
            boolean involves = (username == null) || username.equals(p.getProposerId()) || username.equals(p.getReceiverId());
            if (involves && p.getStatus() == model.domain.enumerations.ProposalStatus.PENDING) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getCompletedProposals(String username) {
        List<Proposal> res = new ArrayList<>();
        for (Proposal p : map.values()) {
            // Avoid using multiple continue/break statements: use structured ifs instead
            if (p == null) {
                // skip null entries
            } else {
                boolean involves = (username == null) || username.equals(p.getProposerId()) || username.equals(p.getReceiverId());
                if (involves) {
                    model.domain.enumerations.ProposalStatus s = p.getStatus();
                    if (s == model.domain.enumerations.ProposalStatus.ACCEPTED
                            || s == model.domain.enumerations.ProposalStatus.REJECTED
                            || s == model.domain.enumerations.ProposalStatus.EXPIRED) {
                        res.add(p);
                    }
                }
            }
        }
        return res;
    }
}
