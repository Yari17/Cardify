package model.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.domain.Card;
import model.domain.PokemonCardDetails;
import model.domain.enumerations.CardGameType;
import net.tcgdex.sdk.TCGdex;
import net.tcgdex.sdk.models.Set;
import net.tcgdex.sdk.models.SetResume;

/**
 * Implementazione del fornitore di carte per l'universo Pokemon.
 * Interagisce con l'API esterna TCGdex tramite il relativo SDK e mantiene una
 * cache locale
 * per ottimizzare le performance ed evitare chiamate ridondanti.
 */
public class PokemonCardProvider implements ICardProvider {
    private static final Logger LOGGER = Logger.getLogger(PokemonCardProvider.class.getName());
    private final TCGdex api;
    private Map<String, List<Card>> cachedCardsBySet;
    private Map<String, String> cachedSetNames;
    private Map<String, Card> cachedCardDetails;

    /**
     * Costruttore predefinito.
     * Inizializza l'API TCGdex con la lingua inglese e predispone le cache.
     */
    public PokemonCardProvider() {
        this.api = new TCGdex("en");
        this.cachedCardsBySet = new HashMap<>();
        this.cachedSetNames = new HashMap<>();
        this.cachedCardDetails = new HashMap<>();
    }

    /**
     * Recupera l'elenco delle carte appartenenti a un determinato set.
     * Utilizza una cache locale per restituire immediatamente i dati se già
     * scaricati.
     * 
     * @param setId L'identificativo univoco del set (es. "swsh1").
     * @return Lista di carte caricate dal servizio esterno o dalla cache.
     * @throws exception.ApiTimeoutException Se il servizio esterno non risponde
     *                                       correttamente.
     */
    @Override
    public List<Card> getCardsBySet(String setId) {
        if (cachedCardsBySet.containsKey(setId)) {
            return cachedCardsBySet.get(setId);
        }

        Set set;
        try {
            set = api.fetchSet(setId);
        } catch (Exception e) {
            throw new exception.ApiTimeoutException(
                    "API failure: External service is not responding while fetching set " + setId, e);
        }

        if (set == null) {
            throw new exception.ApiTimeoutException("API failure: Received null response for set " + setId);
        }

        List<Card> cards = new ArrayList<>();

        set.getCards().forEach(card -> {
            Card newCard = new Card();
            newCard.setCardID(card.getId());
            newCard.setCardName(card.getName());
            newCard.setSetID(set.getId());
            newCard.setGameType(CardGameType.POKEMON);

            String imageUrl = null;
            try {
                String baseImage = card.getImage();
                if (baseImage != null && !baseImage.isEmpty()) {
                    imageUrl = baseImage + "/high.jpg";
                } else {
                    // Nessuna immagine
                }
            } catch (Exception _) {
                LOGGER.log(Level.SEVERE, "Failed to get image URL for card: {0} (ID: {1})",
                        new Object[] { card.getName(), card.getId() });
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                newCard.setImage(imageUrl);
            }
            cards.add(newCard);
        });

        cachedCardsBySet.put(setId, cards);
        return cards;
    }

    /**
     * Arricchisce una carta con i dettagli completi recuperati dall'API
     * (statistiche, abilità, rarità).
     * 
     * @param card L'oggetto carta da arricchire.
     * @return Lo stesso oggetto carta popolato con un
     *         {@link model.domain.PokemonCardDetails}.
     * @throws exception.ApiTimeoutException In caso di errori di comunicazione con
     *                                       l'API.
     */
    @Override
    public Card getCardDetails(Card card) {
        String id = card.getCardID();
        String cardName = card.getCardName();

        LOGGER.info(() -> "=== Richiesta dettagli API per carta: " + cardName + " (ID: " + id + ") ===");

        if (cachedCardDetails.containsKey(id)) {
            LOGGER.log(Level.INFO, "Ritorno dettagli carta dalla cache per: {0}", id);
            return cachedCardDetails.get(id);
        }

        net.tcgdex.sdk.models.Card apiCard;
        try {
            LOGGER.info(() -> "Chiamata API fetchCard per ID: " + id);
            apiCard = api.fetchCard(id);
        } catch (Exception e) {
            throw new exception.ApiTimeoutException(
                    "API failure: External service is not responding while fetching card " + id, e);
        }

        if (apiCard == null) {
            LOGGER.warning(() -> "API ha restituito NULL per carta: " + cardName + " (ID: " + id + ")");
            throw new exception.ApiTimeoutException("API failure: Received null response for card " + id);
        }

        LOGGER.info(() -> "API risposta ricevuta per: " + cardName);
        LOGGER.info(() -> "  - HP: " + apiCard.getHp());
        LOGGER.info(() -> "  - Types: " + apiCard.getTypes());
        LOGGER.info(() -> "  - Stage: " + apiCard.getStage());
        LOGGER.info(() -> "  - Category: " + apiCard.getCategory());
        LOGGER.info(() -> "  - Rarity: " + apiCard.getRarity());
        LOGGER.info(() -> "  - Set ID: " + (apiCard.getSet() != null ? apiCard.getSet().getId() : "null"));

        // Crea e popola PokemonCardDetails
        PokemonCardDetails details = new PokemonCardDetails();
        details.setCategory(apiCard.getCategory());
        details.setLocalId(apiCard.getLocalId());
        details.setIllustrator(apiCard.getIllustrator());
        details.setRarity(apiCard.getRarity());

        // Dettagli del set
        details.setSetId(apiCard.getSet().getId());
        details.setSetName(apiCard.getSet().getName());
        details.setSetLogo(apiCard.getSet().getLogo());
        details.setSetSymbol(apiCard.getSet().getSymbol());

        details.setSetCardCountOfficial(apiCard.getSet().getCardCount().getOfficial());
        details.setSetCardCountTotal(apiCard.getSet().getCardCount().getTotal());

        // Varianti
        details.setVariantFirstEdition(apiCard.getVariants().getFirstEdition());
        details.setVariantHolo(apiCard.getVariants().getHolo());
        details.setVariantNormal(apiCard.getVariants().getNormal());
        details.setVariantReverse(apiCard.getVariants().getReverse());
        details.setVariantWPromo(apiCard.getVariants().getWPromo());

        // Statistiche Pokemon
        details.setHp(apiCard.getHp());
        details.setTypes(apiCard.getTypes());
        details.setEvolveFrom(apiCard.getEvolveFrom());
        details.setDescription(apiCard.getDescription());
        details.setStage(apiCard.getStage());

        // Combattimento - attacchi
        List<Map<String, Object>> attacks = new ArrayList<>();
        // Gli attacchi sono garantiti essere una lista (possibilmente vuota) dal
        // contratto SDK/API
        for (var attack : apiCard.getAttacks()) {
            Map<String, Object> attackMap = new java.util.HashMap<>();
            attackMap.put("name", attack.getName());
            attackMap.put("cost", attack.getCost());
            attackMap.put("damage", attack.getDamage());
            attackMap.put("effect", attack.getEffect());
            attacks.add(attackMap);
        }
        details.setAttacks(attacks);

        // Combattimento - debolezze
        List<Map<String, String>> weaknesses = new ArrayList<>();
        // Le debolezze sono garantite essere una lista (possibilmente vuota) dal
        // contratto SDK/API
        for (var weakness : apiCard.getWeaknesses()) {
            Map<String, String> weaknessMap = new java.util.HashMap<>();
            weaknessMap.put("type", weakness.getType());
            weaknessMap.put("value", weakness.getValue());
            weaknesses.add(weaknessMap);
        }
        details.setWeaknesses(weaknesses);

        details.setRetreat(apiCard.getRetreat());

        // Legalità
        details.setRegulationMark(apiCard.getRegulationMark());
        details.setLegalStandard(apiCard.getLegal().getStandard());
        details.setLegalExpanded(apiCard.getLegal().getExpanded());

        // Salva i dettagli nella carta
        card.setDetails(details);
        cachedCardDetails.put(id, card);

        LOGGER.info(() -> "Dettagli popolati con successo per carta: " + cardName);
        LOGGER.info(() -> "  - PokemonCardDetails creato con HP=" + details.getHp() + ", Stage=" + details.getStage());
        LOGGER.info(() -> "=== Fine caricamento dettagli per " + cardName + " ===");

        return card;
    }

    /**
     * Recupera la mappa dei set disponibili (ID -> Nome).
     * Utilizza la cache se disponibile.
     * 
     * @return Una mappa contenente ID e Nome dei set.
     * @throws exception.ApiTimeoutException In caso di errore API.
     */
    @Override
    public Map<String, String> getSetNameList() {
        if (!cachedSetNames.isEmpty()) {
            LOGGER.log(Level.INFO, "Restituzione nomi set dalla cache");
            return cachedSetNames;
        }

        SetResume[] sets;
        try {
            sets = api.fetchSets();
        } catch (Exception e) {
            throw new exception.ApiTimeoutException(
                    "API failure: External service is not responding while fetching sets", e);
        }

        if (sets == null) {
            throw new exception.ApiTimeoutException("API failure: Received null response for sets");
        }

        for (SetResume set : sets) {
            cachedSetNames.put(set.getId(), set.getName());
        }
        return cachedSetNames;
    }
}
