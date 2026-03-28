package zyo.narutomod.capability;

public interface IShinobiData {
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

    UchihaArchetype getArchetype();
    void setArchetype(UchihaArchetype archetype);

    void copyFrom(IShinobiData source);
}