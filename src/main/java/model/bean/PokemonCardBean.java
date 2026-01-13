package model.bean;


import model.domain.enumerations.CardGameType;

import java.util.List;
import java.util.Map;

public class PokemonCardBean extends CardBean {

    // Basic Info
    private String category;
    private String localId;
    private String illustrator;
    private String rarity;

    // Set Information
    private String setId;
    private String setName;
    private String setLogo;
    private String setSymbol;
    private Integer setCardCountOfficial;
    private Integer setCardCountTotal;

    // Variants
    private Boolean variantFirstEdition;
    private Boolean variantHolo;
    private Boolean variantNormal;
    private Boolean variantReverse;
    private Boolean variantWPromo;

    // Pokemon Specific
    private Integer hp;
    private List<String> types;
    private String evolveFrom;
    private String description;
    private String stage;

    // Battle Stats
    private List<Map<String, Object>> attacks;
    private List<Map<String, String>> weaknesses;
    private Integer retreat;

    // Legal/Regulation
    private String regulationMark;
    private Boolean legalStandard;
    private Boolean legalExpanded;

    // Costruttore di default per Jackson
    public PokemonCardBean() {
        super(null, null, null, CardGameType.POKEMON);
    }

    // Costruttore completo
    public PokemonCardBean(String id, String name, String imageUrl) {
        super(id, name, imageUrl, CardGameType.POKEMON);
    }

    // ========== Getters & Setters ==========

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getSetLogo() {
        return setLogo;
    }

    public void setSetLogo(String setLogo) {
        this.setLogo = setLogo;
    }

    public String getSetSymbol() {
        return setSymbol;
    }

    public void setSetSymbol(String setSymbol) {
        this.setSymbol = setSymbol;
    }

    public Integer getSetCardCountOfficial() {
        return setCardCountOfficial;
    }

    public void setSetCardCountOfficial(Integer setCardCountOfficial) {
        this.setCardCountOfficial = setCardCountOfficial;
    }

    public Integer getSetCardCountTotal() {
        return setCardCountTotal;
    }

    public void setSetCardCountTotal(Integer setCardCountTotal) {
        this.setCardCountTotal = setCardCountTotal;
    }

    public Boolean getVariantFirstEdition() {
        return variantFirstEdition;
    }

    public void setVariantFirstEdition(Boolean variantFirstEdition) {
        this.variantFirstEdition = variantFirstEdition;
    }

    public Boolean getVariantHolo() {
        return variantHolo;
    }

    public void setVariantHolo(Boolean variantHolo) {
        this.variantHolo = variantHolo;
    }

    public Boolean getVariantNormal() {
        return variantNormal;
    }

    public void setVariantNormal(Boolean variantNormal) {
        this.variantNormal = variantNormal;
    }

    public Boolean getVariantReverse() {
        return variantReverse;
    }

    public void setVariantReverse(Boolean variantReverse) {
        this.variantReverse = variantReverse;
    }

    public Boolean getVariantWPromo() {
        return variantWPromo;
    }

    public void setVariantWPromo(Boolean variantWPromo) {
        this.variantWPromo = variantWPromo;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getEvolveFrom() {
        return evolveFrom;
    }

    public void setEvolveFrom(String evolveFrom) {
        this.evolveFrom = evolveFrom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<Map<String, Object>> getAttacks() {
        return attacks;
    }

    public void setAttacks(List<Map<String, Object>> attacks) {
        this.attacks = attacks;
    }

    public List<Map<String, String>> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<Map<String, String>> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public Integer getRetreat() {
        return retreat;
    }

    public void setRetreat(Integer retreat) {
        this.retreat = retreat;
    }

    public String getRegulationMark() {
        return regulationMark;
    }

    public void setRegulationMark(String regulationMark) {
        this.regulationMark = regulationMark;
    }

    public Boolean getLegalStandard() {
        return legalStandard;
    }

    public void setLegalStandard(Boolean legalStandard) {
        this.legalStandard = legalStandard;
    }

    public Boolean getLegalExpanded() {
        return legalExpanded;
    }

    public void setLegalExpanded(Boolean legalExpanded) {
        this.legalExpanded = legalExpanded;
    }

    // ========== Utility Methods ==========

    /**
     * Restituisce il tipo principale della carta (primo della lista)
     */
    public String getPrimaryType() {
        return types != null && !types.isEmpty() ? types.get(0) : "Unknown";
    }

    public boolean isHolo() {
        return Boolean.TRUE.equals(variantHolo);
    }

    public boolean isStandardLegal() {
        return Boolean.TRUE.equals(legalStandard);
    }

}
