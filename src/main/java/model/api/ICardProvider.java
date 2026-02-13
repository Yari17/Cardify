package model.api;

import model.domain.Card;

import java.util.List;
import java.util.Map;

/**
 * Interfaccia che definisce il contratto per i fornitori di dati delle carte.
 * Astrae l'accesso a servizi esterni (API) per recuperare informazioni sui set
 * e dettagli delle carte.
 */
public interface ICardProvider {

     List<Card> getCardsBySet(String setId);
     Card getCardDetails(Card card);
     Map<String, String> getSetNameList();
}
