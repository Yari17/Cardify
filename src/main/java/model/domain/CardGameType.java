package model.domain;

public enum CardGameType {
    POKEMON("Pokemon TCG"),
    MAGIC("Magic: The Gathering");

    private final String displayName;

    CardGameType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
