package zyo.narutomod.logic;

public enum StatType {
    NINJUTSU("Ninjutsu"),
    TAIJUTSU("Taijutsu"),
    KENJUTSU("Kenjutsu"),
    SHURIKENJUTSU("Shurikenjutsu"),
    SUMMONING("Summoning"),
    KINJUTSU("Kinjutsu"),
    SENJUTSU("Senjutsu"),
    MEDICINE("Medicine"),
    SPEED("Speed"),
    JUTSU_POWER("Jutsu-Power"),
    GENJUTSU("Genjutsu"),
    IQ("IQ");

    private final String displayName;

    StatType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}