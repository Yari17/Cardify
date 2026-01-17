package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.dao.IProposalDao;
import model.domain.Proposal;
import exception.DataPersistenceException;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonProposalDao implements IProposalDao {
    private static final Logger LOGGER = Logger.getLogger(JsonProposalDao.class.getName());

    private final String jsonFilePath;
    private final Gson gson;
    private final Map<String, Proposal> proposalsById;
    private final AtomicLong idGenerator;

    public JsonProposalDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) out.nullValue(); else out.value(value.toString());
                    }

                    @Override
                    public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDateTime.parse(in.nextString());
                    }
                })
                .create();

        this.proposalsById = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicLong(0);

        initializeFile();
        loadFromJson();
    }

    private void initializeFile() {
        File file = new File(jsonFilePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        if (!file.exists()) saveToJson();
    }

    private void loadFromJson() {
        File file = new File(jsonFilePath);
        if (!file.exists()) return;
        try (Reader r = new FileReader(file)) {
            Type listType = new TypeToken<List<Proposal>>(){}.getType();
            List<Proposal> list = gson.fromJson(r, listType);
            if (list != null) {
                long max = 0;
                for (Proposal p : list) {
                    proposalsById.put(p.getProposalId(), p);
                    try {
                        long numeric = Long.parseLong(p.getProposalId());
                        if (numeric > max) max = numeric;
                    } catch (NumberFormatException _) {}
                }
                idGenerator.set(max);
                LOGGER.log(Level.INFO, "Loaded {0} proposals from JSON", list.size());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading proposals JSON, starting empty", e);
            saveToJson();
        }
    }

    private void saveToJson() {
        try (Writer w = new FileWriter(jsonFilePath)) {
            List<Proposal> list = new ArrayList<>(proposalsById.values());
            gson.toJson(list, w);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to save proposals to " + jsonFilePath, e);
        }
    }

    @Override
    public List<Proposal> getAll() {
        return new ArrayList<>(proposalsById.values());
    }

    @Override
    public Optional<Proposal> getById(String proposalId) {
        if (proposalId == null) return Optional.empty();
        return Optional.ofNullable(proposalsById.get(proposalId));
    }

    @Override
    public void save(Proposal proposal) {
        if (proposal == null) throw new IllegalArgumentException("Proposal cannot be null");
        if (proposal.getProposalId() == null || proposal.getProposalId().isEmpty()) {
            String id = String.valueOf(idGenerator.incrementAndGet());
            proposal.setProposalId(id);
        }
        proposal.setLastUpdated(LocalDateTime.now());
        proposalsById.put(proposal.getProposalId(), proposal);
        saveToJson();
        LOGGER.log(Level.INFO, "Saved proposal {0}", proposal.getProposalId());
    }

    @Override
    public void update(Proposal proposal) {
        if (proposal == null || proposal.getProposalId() == null) throw new IllegalArgumentException("Invalid proposal");
        proposal.setLastUpdated(LocalDateTime.now());
        proposalsById.put(proposal.getProposalId(), proposal);
        saveToJson();
        LOGGER.log(Level.INFO, "Updated proposal {0}", proposal.getProposalId());
    }

    @Override
    public void delete(Proposal proposal) {
        if (proposal == null || proposal.getProposalId() == null) throw new IllegalArgumentException("Invalid proposal");
        proposalsById.remove(proposal.getProposalId());
        saveToJson();
        LOGGER.log(Level.INFO, "Deleted proposal {0}", proposal.getProposalId());
    }

    @Override
    public List<Proposal> getSentPendingProposal(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : proposalsById.values()) {
            if (p == null) continue;
            if (username != null && username.equals(p.getProposerId())) {
                if (p.getStatus() == model.domain.enumerations.ProposalStatus.PENDING || p.getStatus() == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getReceivedProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : proposalsById.values()) {
            if (p == null) continue;
            if (username != null && username.equals(p.getReceiverId())) {
                if (p.getStatus() == model.domain.enumerations.ProposalStatus.PENDING || p.getStatus() == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getScheduledProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : proposalsById.values()) {
            if (p == null) continue;
            // Scheduled proposals are those accepted (or later) involving the user
            if ((username != null && (username.equals(p.getProposerId()) || username.equals(p.getReceiverId())))
                    && p.getStatus() == model.domain.enumerations.ProposalStatus.ACCEPTED) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getPendingProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : proposalsById.values()) {
            if (p == null) continue;
            if (username != null && (username.equals(p.getProposerId()) || username.equals(p.getReceiverId()))) {
                if (p.getStatus() == model.domain.enumerations.ProposalStatus.PENDING) result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Proposal> getCompletedProposals(String username) {
        List<Proposal> result = new ArrayList<>();
        for (Proposal p : proposalsById.values()) {
            if (p == null) continue;
            if (username != null && (username.equals(p.getProposerId()) || username.equals(p.getReceiverId()))) {
                model.domain.enumerations.ProposalStatus s = p.getStatus();
                if (s == model.domain.enumerations.ProposalStatus.ACCEPTED
                        || s == model.domain.enumerations.ProposalStatus.REJECTED
                        || s == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    result.add(p);
                }
            }
        }
        return result;
    }
}
