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
            LOGGER.log(Level.INFO,"SDK returned: {0} sets ",(setArray != null ? setArray.length : 0));

            Map<String, String> setMap = new HashMap<>();

            if (setArray != null) {
                for (SetResume setResume : setArray) {
                    setMap.put(setResume.getId(), setResume.getName());
                }
                LOGGER.log(Level.INFO,"Successfully mapped {0}  sets", setMap.size());
            } else {
                LOGGER.warning("fetchSets() returned null!");
            }
            return setMap;
        } catch (Exception e) {
            LOGGER.severe("Exception in getAllSets: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }


    @Override
    public PokemonCard getCardDetails(String cardId) {

        try {
            LOGGER.log(java.util.logging.Level.INFO, "Fetching card details for ID: {0}", cardId);
            net.tcgdex.sdk.models.Card card = api.fetchCard(cardId);

            if (card == null) {
                LOGGER.log(java.util.logging.Level.WARNING, "API returned null for card ID: {0}", cardId);
                return null;
            }

            // Crea PokemonCard con i dati base
            String imageUrl = card.getImage() != null ? card.getImage() + HIGH_PNG_SUFFIX : null;
            PokemonCard pokemonCard = new PokemonCard(card.getId(), card.getName(), imageUrl);

            // Imposta i dati base della carta
            pokemonCard.setCategory(card.getCategory());
            pokemonCard.setLocalId(card.getLocalId());
            pokemonCard.setIllustrator(card.getIllustrator());
            pokemonCard.setRarity(card.getRarity());

            // Imposta i dati del set (con null-check)
            if (card.getSet() != null) {
                pokemonCard.setSetId(card.getSet().getId());
                pokemonCard.setSetName(card.getSet().getName());
                pokemonCard.setSetLogo(card.getSet().getLogo());
                pokemonCard.setSetSymbol(card.getSet().getSymbol());
            }

            // Imposta le varianti (con null-check)
            if (card.getVariants() != null) {
                pokemonCard.setVariantFirstEdition(card.getVariants().getFirstEdition());
                pokemonCard.setVariantHolo(card.getVariants().getHolo());
                pokemonCard.setVariantNormal(card.getVariants().getNormal());
                pokemonCard.setVariantReverse(card.getVariants().getReverse());
                pokemonCard.setVariantWPromo(card.getVariants().getWPromo());
            }

            // Imposta i dati del Pokemon (se disponibili)
            pokemonCard.setHp(card.getHp());
            pokemonCard.setTypes(card.getTypes());
            pokemonCard.setEvolveFrom(card.getEvolveFrom());
            pokemonCard.setDescription(card.getDescription());
            pokemonCard.setStage(card.getStage());

            // Converti attacks da List<CardAttack> a List<Map<String, Object>> (con
            // null-check)
            var attacksRaw = card.getAttacks();
            if (attacksRaw != null && !attacksRaw.isEmpty()) {
                List<Map<String, Object>> attacksList = new ArrayList<>();
                for (var attack : attacksRaw) {
                    if (attack != null) {
                        Map<String, Object> attackMap = new HashMap<>();
                        // Normalize fields to avoid nulls later in the view (null-safe check)
                        String attackName = attack.getName();
                        attackMap.put("name", (attackName != null && !attackName.isEmpty()) ? attackName : "Unknown");
                        attackMap.put("cost", attack.getCost());
                        // Damage may be string or numeric; normalize to String (empty if null)
                        Object dmg = attack.getDamage();
                        attackMap.put("damage", dmg != null ? dmg.toString() : "");
                        // Effect may be null; normalize to empty string if absent
                        attackMap.put("effect", attack.getEffect() != null ? attack.getEffect() : "");
                        attacksList.add(attackMap);
                    }
                }
                pokemonCard.setAttacks(attacksList);
            }

            // Converti weaknesses da List<CardWeakRes> a List<Map<String, String>> (con
            // null-check)
            var weaknessesRaw = card.getWeaknesses();
            if (weaknessesRaw != null && !weaknessesRaw.isEmpty()) {
                List<Map<String, String>> weaknessesList = new ArrayList<>();
                for (var weakness : weaknessesRaw) {
                    if (weakness != null) {
                        Map<String, String> weaknessMap = new HashMap<>();
                        weaknessMap.put("type", weakness.getType());
                        weaknessMap.put("value", weakness.getValue());
                        weaknessesList.add(weaknessMap);
                    }
                }
                pokemonCard.setWeaknesses(weaknessesList);
            }

            pokemonCard.setRetreat(card.getRetreat());

            // Imposta i dati legali (con null-check)
            pokemonCard.setRegulationMark(card.getRegulationMark());
            if (card.getLegal() != null) {
                pokemonCard.setLegalStandard(card.getLegal().getStandard());
                pokemonCard.setLegalExpanded(card.getLegal().getExpanded());
            }

            LOGGER.log(Level.INFO, "Successfully loaded details for card: {0}", cardId);
            return pokemonCard;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading card details for ID {0}: {1} - {2}",
                    new Object[]{cardId, e.getClass().getName(), e.getMessage()});
            // log stack trace at finer level
            LOGGER.log(java.util.logging.Level.FINER, "Exception", e);
            return null;
        }
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
