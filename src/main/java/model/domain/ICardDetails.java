package model.domain;

/**
 * Interfaccia base per i dettagli specifici di una carta.
 * Permette di supportare dinamicamente diversi tipi di gioco (Pokemon, Magic,
 * ecc.)
 * fornendo un contratto comune per gli attributi estesi.
 */
public interface ICardDetails {
    // Utilizzata per le varie implementazioni e per supportare in modo dinamico
    // dettagli specifici di carte di diversi giochi
}
