package model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AppConfig;
import model.bean.MagicCardBean;
import model.bean.PokemonCardBean;
import model.domain.CardGameType;
import model.domain.card.Card;
import model.domain.card.PokemonCard;
import model.service.mapper.MagicCardMapper;
import model.service.mapper.PokemonCardMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CardApiService {
    private static final Logger LOGGER = Logger.getLogger(CardApiService.class.getName());

    private static final String POKEMON_BASE_URL = "https://api.pokemontcg.io/v2";
    private static final String MAGIC_BASE_URL = "https://api.magicthegathering.io/v1";
    private static final String CARDS_ENDPOINT = "/cards";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final PokemonCardMapper pokemonMapper;
    private final MagicCardMapper magicMapper;

    public CardApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.pokemonMapper = new PokemonCardMapper();
        this.magicMapper = new MagicCardMapper();
    }

    public List<Card> searchCardsByName(String cardName, CardGameType gameType) throws IOException, InterruptedException {
        return switch (gameType) {
            case POKEMON -> searchPokemonCardsByName(cardName);
            case MAGIC -> searchMagicCardsByName(cardName);
        };
    }

    public Card getCardById(String cardId, CardGameType gameType) throws IOException, InterruptedException {
        return switch (gameType) {
            case POKEMON -> getPokemonCardById(cardId);
            case MAGIC -> throw new UnsupportedOperationException("Magic API doesn't support single card fetch by ID");
        };
    }

    public List<Card> searchCardsByType(String type, CardGameType gameType) throws IOException, InterruptedException {
        return switch (gameType) {
            case POKEMON -> searchPokemonCardsByType(type);
            case MAGIC -> searchMagicCardsByType(type);
        };
    }

    private List<Card> searchPokemonCardsByName(String cardName) throws IOException, InterruptedException {
        String query = String.format("name:\"%s\"", cardName);
        return searchPokemonCards(query);
    }

    private PokemonCard getPokemonCardById(String cardId) throws IOException, InterruptedException {
        String url = POKEMON_BASE_URL + CARDS_ENDPOINT + "/" + cardId;

        HttpRequest request = buildPokemonRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response, "Pokemon card");

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode dataNode = rootNode.get("data");

        PokemonCardBean bean = objectMapper.treeToValue(dataNode, PokemonCardBean.class);
        return pokemonMapper.toDomain(bean);
    }

    private List<Card> searchPokemonCards(String query) throws IOException, InterruptedException {
        String url = POKEMON_BASE_URL + CARDS_ENDPOINT + "?q=" + query.replace(" ", "%20");

        HttpRequest request = buildPokemonRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response, "Pokemon cards");

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode dataArray = rootNode.get("data");

        List<Card> cards = new ArrayList<>();
        if (dataArray != null && dataArray.isArray()) {
            for (JsonNode node : dataArray) {
                PokemonCardBean bean = objectMapper.treeToValue(node, PokemonCardBean.class);
                cards.add(pokemonMapper.toDomain(bean));
            }
        }

        return cards;
    }

    private List<Card> searchPokemonCardsByType(String type) throws IOException, InterruptedException {
        String query = "types:" + type;
        return searchPokemonCards(query);
    }

    public List<Card> getPokemonCardsBySet(String setId) throws IOException, InterruptedException {
        String query = "set.id:" + setId;
        return searchPokemonCards(query);
    }

    private List<Card> searchMagicCardsByName(String cardName) throws IOException, InterruptedException {
        String url = MAGIC_BASE_URL + CARDS_ENDPOINT + "?name=" + cardName.replace(" ", "%20");
        return fetchMagicCards(url);
    }

    private List<Card> searchMagicCardsByType(String type) throws IOException, InterruptedException {
        String url = MAGIC_BASE_URL + CARDS_ENDPOINT + "?type=" + type.replace(" ", "%20");
        return fetchMagicCards(url);
    }

    public List<Card> getMagicCardsBySet(String setCode) throws IOException, InterruptedException {
        String url = MAGIC_BASE_URL + CARDS_ENDPOINT + "?set=" + setCode;
        return fetchMagicCards(url);
    }

    public List<Card> getMagicCardsByColors(String colors) throws IOException, InterruptedException {
        String url = MAGIC_BASE_URL + CARDS_ENDPOINT + "?colors=" + colors;
        return fetchMagicCards(url);
    }

    private List<Card> fetchMagicCards(String url) throws IOException, InterruptedException {
        HttpRequest request = buildMagicRequest(url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response, "Magic cards");

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode cardsArray = rootNode.get("cards");

        List<Card> cards = new ArrayList<>();
        if (cardsArray != null && cardsArray.isArray()) {
            for (JsonNode node : cardsArray) {
                MagicCardBean bean = objectMapper.treeToValue(node, MagicCardBean.class);
                cards.add(magicMapper.toDomain(bean));
            }
        }

        return cards;
    }


    private HttpRequest buildPokemonRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Api-Key", AppConfig.API_KEY.trim())
            .GET()
            .build();
    }

    private HttpRequest buildMagicRequest(String url) {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
    }

    private void validateResponse(HttpResponse<String> response, String context) throws IOException {
        if (response.statusCode() != 200) {
            LOGGER.log(Level.WARNING, "API returned status code: {0} for {1}",
                new Object[]{response.statusCode(), context});
            throw new IOException("Failed to fetch " + context + ": HTTP " + response.statusCode());
        }
    }
}

