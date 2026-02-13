package model.bean;

import model.domain.CollectionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean che rappresenta un raccoglitore di carte nel sistema.
 * Utilizzato per trasportare i dati sintetici di un'intera collezione di un set
 * specifico.
 */
public class BinderBean {
    /** Nome utente del proprietario del raccoglitore. */
    private String owner;
    /** ID unico del set di carte contenuto. */
    private String setID;
    /** Nome leggibile del set. */
    private String setName;
    /** Lista delle carte effettivamente possedute nell'ambito di questo set. */
    private List<CollectionItem> ownedCards = new ArrayList<>();

    /**
     * Costruttore vuoto per compatibilità JavaBean.
     */
    public BinderBean() {
        // Costruttore vuoto intenzionale per framework di serializzazione/mapping
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSetID() {
        return setID;
    }

    public void setSetID(String setID) {
        this.setID = setID;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public List<CollectionItem> getOwnedCards() {
        return ownedCards;
    }

    public void setOwnedCards(List<CollectionItem> ownedCards) {
        this.ownedCards = ownedCards != null ? ownedCards : new ArrayList<>();
    }
}
