package model.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.domain.enumerations.CardGameType;
import model.domain.Card;
import model.domain.PokemonCard;
import net.tcgdex.sdk.TCGdex;
import net.tcgdex.sdk.models.CardResume;
import net.tcgdex.sdk.models.Set;
import net.tcgdex.sdk.models.SetResume;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PokemonCardProvider implements ICardProvider {
    private static final Logger LOGGER = Logger.getLogger(PokemonCardProvider.class.getName());
    private final TCGdex api;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String IMAGE_FIELD = "image";
    private static final String HIGH_PNG_SUFFIX = "/high.png";

    public PokemonCardProvider() {
        this.api = new TCGdex("en");
    }

    @Override
    public List<Card> searchSet(String setId) {
        try {
            Set set = api.fetchSet(setId);
            assert set != null;
            List<CardResume> cards = set.getCards();

            List<Card> cardList = new ArrayList<>();
            for (CardResume cardResume : cards) {
                String imageUrl = cardResume.getImage() != null ? cardResume.getImage() + HIGH_PNG_SUFFIX : null;

                Card card = new Card(
                        cardResume.getId(),
                        cardResume.getName(),
                        imageUrl,
                        CardGameType.POKEMON);
                cardList.add(card);
            }

            return cardList;
        } catch (NullPointerException _) {
            return new ArrayList<>();
        }
    }

    // l'sdk non fornisce un metodo per cercare le carte per nome, quindi devo fare
    // una richiesta http
    @Override
    public List<Card> searchCardsByName(String cardName) {
        try {
            // Costruisci l'URL con il parametro di ricerca
            String encodedName = URLEncoder.encode(cardName, StandardCharsets.UTF_8);
            String url = "https://api.tcgdex.net/v2/en/cards?name=" + encodedName;

            // Effettua la richiesta HTTP GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Parse del JSON response direttamente in Card usando JsonArray
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(response.body(), JsonArray.class);

            return getCards(jsonArray);
        } catch (InterruptedException _) {
            // Re-interrompi il thread per preservare lo stato di interruzione
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } catch (Exception _) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, String> getAllSets() {
        try {
            LOGGER.info("PokemonAdapter.getAllSets() called - fetching sets from SDK...");
            SetResume[] setArray = api.fetchSets();
            LOGGER.log(Level.INFO, "SDK returned: {0} sets ", (setArray != null ? setArray.length : 0));

            Map<String, String> setMap = new HashMap<>();

            if (setArray != null) {
                for (SetResume setResume : setArray) {
                    setMap.put(setResume.getId(), setResume.getName());
                }
                LOGGER.log(Level.INFO, "Successfully mapped {0}  sets", setMap.size());
            } else {
                LOGGER.warning("fetchSets() returned null!");
            }
            return setMap;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception in getAllSets", ex);
            return new HashMap<>();
        }
    }



    @Override
    public <T extends Card> T getCardDetails(String cardId) {

        try {
            LOGGER.log(java.util.logging.Level.INFO, "Fetching card details for ID: {0}", cardId);
            net.tcgdex.sdk.models.Card card = api.fetchCard(cardId);

            if (card == null) {
                LOGGER.log(java.util.logging.Level.WARNING, "API returned null for card ID: {0}", cardId);
                return null;
            }

            PokemonCard pokemonCard = buildPokemonCardFromApi(card);
            populateAttacks(card, pokemonCard);
            populateWeaknesses(card, pokemonCard);

            LOGGER.log(Level.INFO, "Successfully loaded details for card: {0}", cardId);
            return (T) pokemonCard;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading card details for ID " + cardId, ex);
            // log stack trace at finer level
            LOGGER.log(java.util.logging.Level.FINER, "Exception", ex);
            return null;
        }
    }

    // Helper: create and populate the basic PokemonCard fields from the SDK model
    private PokemonCard buildPokemonCardFromApi(net.tcgdex.sdk.models.Card card) {
        String imageUrl = card.getImage() != null ? card.getImage() + HIGH_PNG_SUFFIX : null;
        PokemonCard pc = new PokemonCard(card.getId(), card.getName(), imageUrl);

        pc.setCategory(card.getCategory());
        pc.setLocalId(card.getLocalId());
        pc.setIllustrator(card.getIllustrator());
        pc.setRarity(card.getRarity());


        pc.setSetId(card.getSet().getId());
        pc.setSetName(card.getSet().getName());
        pc.setSetLogo(card.getSet().getLogo());
        pc.setSetSymbol(card.getSet().getSymbol());


        if (card.getVariants() != null) {
            pc.setVariantFirstEdition(card.getVariants().getFirstEdition());
            pc.setVariantHolo(card.getVariants().getHolo());
            pc.setVariantNormal(card.getVariants().getNormal());
            pc.setVariantReverse(card.getVariants().getReverse());
            pc.setVariantWPromo(card.getVariants().getWPromo());
        }

        pc.setHp(card.getHp());
        pc.setTypes(card.getTypes());
        pc.setEvolveFrom(card.getEvolveFrom());
        pc.setDescription(card.getDescription());
        pc.setStage(card.getStage());
        pc.setRetreat(card.getRetreat());
        pc.setRegulationMark(card.getRegulationMark());

        pc.setLegalStandard(card.getLegal().getStandard());
        pc.setLegalExpanded(card.getLegal().getExpanded());


        return pc;
    }

    // Helper: convert and attach attacks to the PokemonCard
    private void populateAttacks(net.tcgdex.sdk.models.Card card, PokemonCard pc) {
        var attacksRaw = card.getAttacks();
        if (attacksRaw.isEmpty()) return;
        List<Map<String, Object>> attacksList = new ArrayList<>();
        for (var attack : attacksRaw) {
            if (attack == null) continue;
            Map<String, Object> attackMap = new HashMap<>();
            String attackName = attack.getName();
            attackMap.put("name", (!attackName.isEmpty()) ? attackName : "Unknown");
            attackMap.put("cost", attack.getCost());
            Object dmg = attack.getDamage();
            attackMap.put("damage", dmg != null ? dmg.toString() : "");
            attackMap.put("effect", attack.getEffect() != null ? attack.getEffect() : "");
            attacksList.add(attackMap);
        }
        pc.setAttacks(attacksList);
    }

    // Helper: convert and attach weaknesses to the PokemonCard
    private void populateWeaknesses(net.tcgdex.sdk.models.Card card, PokemonCard pc) {
        var weaknessesRaw = card.getWeaknesses();
        if (weaknessesRaw.isEmpty()) return;
        List<Map<String, String>> weaknessesList = new ArrayList<>();
        for (var weakness : weaknessesRaw) {
            if (weakness == null) continue;
            Map<String, String> weaknessMap = new HashMap<>();
            weaknessMap.put("type", weakness.getType());
            weaknessMap.put("value", weakness.getValue());
            weaknessesList.add(weaknessMap);
        }
        pc.setWeaknesses(weaknessesList);
    }

    private List<Card> getCards(JsonArray jsonArray) {
        List<Card> cardList = new ArrayList<>();
        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                JsonObject jsonCard = element.getAsJsonObject();

                String id = jsonCard.get("id").getAsString();
                String cardName = jsonCard.get("name").getAsString();
                String imageUrl = jsonCard.has(IMAGE_FIELD) && !jsonCard.get(IMAGE_FIELD).isJsonNull()
                        ? jsonCard.get(IMAGE_FIELD).getAsString() + HIGH_PNG_SUFFIX
                        : null;

                Card card = new Card(id, cardName, imageUrl, CardGameType.POKEMON);
                cardList.add(card);
            }
        }
        return cardList;
    }
}
