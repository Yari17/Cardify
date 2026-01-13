package model.domain;

import model.bean.PokemonCardBean;

import java.util.List;
import java.util.Map;

public class PokemonCard extends Card {

    private String category;
    private String localId;
    private String illustrator;
    private String rarity;

    private String setId;
    private String setName;
    private String setLogo;
    private String setSymbol;
    private Integer setCardCountOfficial;
    private Integer setCardCountTotal;

    private Boolean variantFirstEdition;
    private Boolean variantHolo;
    private Boolean variantNormal;
    private Boolean variantReverse;
    private Boolean variantWPromo;

    private Integer hp;
    private List<String> types;
    private String evolveFrom;
    private String description;
    private String stage;

    private List<Map<String, Object>> attacks;
    private List<Map<String, String>> weaknesses;
    private Integer retreat;

    private String regulationMark;
    private Boolean legalStandard;
    private Boolean legalExpanded;

    public PokemonCard(String id, String name, String imageUrl) {
        super(id, name, imageUrl, CardGameType.POKEMON);
    }

    @Override
    public PokemonCardBean toBean() {
        PokemonCardBean bean = new PokemonCardBean(
                super.id,
                super.name,
                super.imageUrl
        );

        bean.setCategory(category);
        bean.setLocalId(localId);
        bean.setIllustrator(illustrator);
        bean.setRarity(rarity);

        bean.setSetId(setId);
        bean.setSetName(setName);
        bean.setSetLogo(setLogo);
        bean.setSetSymbol(setSymbol);
        bean.setSetCardCountOfficial(setCardCountOfficial);
        bean.setSetCardCountTotal(setCardCountTotal);

        bean.setVariantFirstEdition(variantFirstEdition);
        bean.setVariantHolo(variantHolo);
        bean.setVariantNormal(variantNormal);
        bean.setVariantReverse(variantReverse);
        bean.setVariantWPromo(variantWPromo);

        bean.setHp(hp);
        bean.setTypes(types);
        bean.setEvolveFrom(evolveFrom);
        bean.setDescription(description);
        bean.setStage(stage);

        bean.setAttacks(attacks);
        bean.setWeaknesses(weaknesses);
        bean.setRetreat(retreat);

        bean.setRegulationMark(regulationMark);
        bean.setLegalStandard(legalStandard);
        bean.setLegalExpanded(legalExpanded);

        return bean;
    }

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

    public String getPrimaryType() {
        return types != null && !types.isEmpty() ? types.get(0) : "Unknown";
    }

    public boolean isHolo() {
        return Boolean.TRUE.equals(variantHolo);
    }

    public boolean isStandardLegal() {
        return Boolean.TRUE.equals(legalStandard);
    }

    public boolean hasEvolution() {
        return evolveFrom != null && !evolveFrom.isEmpty();
    }

    public int getAttackCount() {
        return attacks != null ? attacks.size() : 0;
    }

    public int getWeaknessCount() {
        return weaknesses != null ? weaknesses.size() : 0;
    }

    public boolean isBasicPokemon() {
        return "BASIC".equalsIgnoreCase(stage);
    }

    @Override
    public String toString() {
        return "PokemonCard{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", hp=" + hp +
                ", types=" + types +
                ", rarity='" + rarity + '\'' +
                ", set='" + setName + '\'' +
                '}';
    }
}
