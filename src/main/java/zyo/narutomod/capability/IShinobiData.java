package zyo.narutomod.capability;

import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Archetype;
import zyo.narutomod.player.Village;

public interface IShinobiData {
    java.util.List<String> getUnlockedJutsus();

    java.util.Map<String, Integer> getActiveCooldowns();
    void setCooldown(String jutsuId, int ticks);
    boolean isOnCooldown(String jutsuId);
    void tickCooldowns();

    float getChakra();
    float getMaxChakra();
    void setChakra(float chakra);

    int getSharinganStage();
    void setSharinganStage(int stage);

    boolean isSharinganActive();
    void setSharinganActive(boolean active);

    boolean isInKamuiDimension();
    void setInKamuiDimension(boolean inDimension);

    String getMsVariant();
    void setMsVariant(String variant);

    int getNinjutsuStat();
    void setNinjutsuStat(int level);

    int getGenjutsuStat();
    void setGenjutsuStat(int level);

    boolean isCloneInfusionReady();
    void setCloneInfusionReady(boolean ready);

    Clan getClan();
    void setClan(Clan clan);

    Archetype getArchetype();
    void setArchetype(Archetype archetype);

    Village getVillage();
    void setVillage(Village village);

    void unlockJutsu(String jutsuId);
    boolean hasJutsu(String jutsuId);

    String getEquippedJutsu(int slotId);
    void setEquippedJutsu(int slotId, String jutsuId);

    int getNatureMastery(String nature);
    void addNatureMastery(String nature, int amount);

    int getExperience();
    void setExperience(int xp);

    boolean isSusanooActive();
    void setSusanooActive(boolean active);

    int getMsBleedTimer();
    void setMsBleedTimer(int ticks);

    boolean isChidoriActive();
    void setChidoriActive(boolean active);

    void copyFrom(IShinobiData source);
}