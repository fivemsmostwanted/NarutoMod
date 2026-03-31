package zyo.narutomod.player;

public enum Village {
    NONE("Rogue Ninja"),
    LEAF("Hidden Leaf Village"),
    SAND("Hidden Sand Village"),
    MIST("Hidden Mist Village"),
    CLOUD("Hidden Cloud Village"),
    STONE("Hidden Stone Village"),
    SOUND("Hidden Sound Village"),
    RAIN("Hidden Rain Village");

    private final String displayName;

    Village(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}