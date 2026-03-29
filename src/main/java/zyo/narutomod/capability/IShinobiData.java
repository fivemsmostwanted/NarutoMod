package zyo.narutomod.capability;

import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Archetype;
import zyo.narutomod.player.Village;

public interface IShinobiData {
    java.util.List<String> getUnlockedJutsus();

    float getChakra();
    float getMaxChakra();
    void setChakra(float chakra);

    int getSharinganStage();
    void setSharinganStage(int stage);

    boolean isSharinganActive();
    void setSharinganActive(boolean active);

    boolean isInKamuiDimension();
    void setInKamuiDimension(boolean inDimension);

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

    void copyFrom(IShinobiData source);
}