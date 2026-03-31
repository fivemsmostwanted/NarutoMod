package zyo.narutomod.player;

import java.util.Arrays;
import java.util.List;

public enum Clan {
    CLANLESS("Clanless", Arrays.asList(Archetype.BRAWLER, Archetype.MEDICAL, Archetype.NONE)),
    UCHIHA("Uchiha", Arrays.asList(Archetype.DESTROYER, Archetype.ILLUSIONIST)),
    UZUMAKI("Uzumaki", Arrays.asList(Archetype.SEAL_MASTER, Archetype.JINCHURIKI));

    private final String displayName;
    private final List<Archetype> availableArchetypes;

    Clan(String displayName, List<Archetype> availableArchetypes) {
        this.displayName = displayName;
        this.availableArchetypes = availableArchetypes;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<Archetype> getAvailableArchetypes() {
        return this.availableArchetypes;
    }

    public boolean canUseArchetype(Archetype archetype) {
        return this.availableArchetypes.contains(archetype);
    }
}