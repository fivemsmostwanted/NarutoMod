package zyo.narutomod.capability;

public interface IShinobiData {
    float getChakra();
    void setChakra(float chakra);

    int getSharinganStage();
    void setSharinganStage(int stage);

    boolean isSharinganActive();
    void setSharinganActive(boolean active);

    boolean isInKamuiDimension();
    void setInKamuiDimension(boolean inDimension);

    // NEW: RPG Stats for Scaling
    int getNinjutsuStat();
    void setNinjutsuStat(int level);

    int getGenjutsuStat();
    void setGenjutsuStat(int level);

    void copyFrom(IShinobiData source);
}