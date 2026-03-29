package zyo.narutomod.player;

public enum Archetype {
    NONE("None"),

    DESTROYER("Destroyer"),
    ILLUSIONIST("Illusionist"),

    SEAL_MASTER("Seal Master"),
    JINCHURIKI("Jinchuriki"),

    BRAWLER("Taijutsu Brawler"),
    MEDICAL("Medical Ninja");

    private final String displayName;

    Archetype(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}