package model.domain.card.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.domain.card.Card;
import model.domain.card.PokemonCard;
import model.domain.CardGameType;
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

public class PokemonAdapter implements ICardApiAdapter<PokemonCard> {
    private final TCGdex api;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String IMAGE_FIELD = "image";
    private static final String HIGH_PNG_SUFFIX = "/high.png";

    public PokemonAdapter() {
        this.api = new TCGdex("en");
    }


    @Override
    public List<Card> searchCardsByName(String name) {
        try {
            // Costruisci l'URL con il parametro di ricerca
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
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

    @Override
    public List<Card> searchSet(String setID) {
        try {
            Set set = api.fetchSet(setID);
            assert set != null;
            List<CardResume> cards = set.getCards();

            List<Card> cardList = new ArrayList<>();
            for (CardResume cardResume : cards) {
                String imageUrl = cardResume.getImage() != null ?
                        cardResume.getImage() + HIGH_PNG_SUFFIX : null;

                Card card = new Card(
                        cardResume.getId(),
                        cardResume.getName(),
                        imageUrl,
                        CardGameType.POKEMON
                );
                cardList.add(card);
            }

            return cardList;
        } catch (NullPointerException _) {
            return new ArrayList<>();
        }
    }

    @Override
    public Set getSetDetails(String setID) {
        return api.fetchSet(setID);
    }

    @Override
    public Card getCardById(String id) {
        return null;
    }

    @Override
    public PokemonCard getCardDetails(String id) {
        try {
            net.tcgdex.sdk.models.Card card = api.fetchCard(id);

            if (card == null) {
                return null;
            }

            // Crea PokemonCard con i dati base
            String imageUrl = card.getImage() != null ? card.getImage() + HIGH_PNG_SUFFIX : null;
            PokemonCard pokemonCard = new PokemonCard(card.getId(), card.getName(), imageUrl);

            // Imposta i dati base della carta
            pokemonCard.setCategory(card.getCategory());
            pokemonCard.setLocalId(card.getLocalId());
            pokemonCard.setIllustrator(card.getIllustrator());
            pokemonCard.setRarity(card.getRarity()); // getRarity() restituisce già String

            // Imposta i dati del set (SetResume contiene solo id, name, logo, symbol)
            pokemonCard.setSetId(card.getSet().getId());
            pokemonCard.setSetName(card.getSet().getName());
            pokemonCard.setSetLogo(card.getSet().getLogo());
            pokemonCard.setSetSymbol(card.getSet().getSymbol());
            // cardCountOfficial e cardCountTotal non sono disponibili in SetResume

            // Imposta le varianti
            pokemonCard.setVariantFirstEdition(card.getVariants().getFirstEdition());
            pokemonCard.setVariantHolo(card.getVariants().getHolo());
            pokemonCard.setVariantNormal(card.getVariants().getNormal());
            pokemonCard.setVariantReverse(card.getVariants().getReverse());
            pokemonCard.setVariantWPromo(card.getVariants().getWPromo());

            // Imposta i dati del Pokemon (se disponibili)
            pokemonCard.setHp(card.getHp());
            pokemonCard.setTypes(card.getTypes());
            pokemonCard.setEvolveFrom(card.getEvolveFrom());
            pokemonCard.setDescription(card.getDescription());
            pokemonCard.setStage(card.getStage());

            // Converti attacks da List<CardAttack> a List<Map<String, Object>>
            List<Map<String, Object>> attacksList = new ArrayList<>();
            for (var attack : card.getAttacks()) {
                Map<String, Object> attackMap = new HashMap<>();
                attackMap.put("name", attack.getName());
                attackMap.put("cost", attack.getCost());
                attackMap.put("damage", attack.getDamage());
                attackMap.put("effect", attack.getEffect());
                attacksList.add(attackMap);
            }
            pokemonCard.setAttacks(attacksList);

            // Converti weaknesses da List<CardWeakRes> a List<Map<String, String>>
            List<Map<String, String>> weaknessesList = new ArrayList<>();
            for (var weakness : card.getWeaknesses()) {
                Map<String, String> weaknessMap = new HashMap<>();
                weaknessMap.put("type", weakness.getType());
                weaknessMap.put("value", weakness.getValue());
                weaknessesList.add(weaknessMap);
            }
            pokemonCard.setWeaknesses(weaknessesList);

            pokemonCard.setRetreat(card.getRetreat());

            // Imposta i dati legali
            pokemonCard.setRegulationMark(card.getRegulationMark());
            pokemonCard.setLegalStandard(card.getLegal().getStandard());
            pokemonCard.setLegalExpanded(card.getLegal().getExpanded());

            return pokemonCard;
        } catch (Exception _) {
            return null;
        }
    }

    @Override
    public Map<String, String> getAllSets() {
        try {
            SetResume[] setArray = api.fetchSets();
            Map<String, String> setMap = new HashMap<>();

            if (setArray != null) {
                for (SetResume setResume : setArray) {
                    setMap.put(setResume.getId(), setResume.getName());
                }
            }
            return setMap;
        } catch (Exception _) {
            return new HashMap<>();
        }
    }
}
