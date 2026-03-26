package zyo.narutomod.capability;

import net.minecraft.nbt.CompoundTag;

public class ShinobiData implements IShinobiData {
    private float chakra = 100.0f;
    private int sharinganStage = 0;
    private boolean sharinganActive = false;
    private boolean inKamui = false;

    // NEW: Default starting stats (Level 1)
    private int ninjutsuStat = 1;
    private int genjutsuStat = 1;

    // Existing Chakra/Sharingan/Kamui methods...
    @Override public float getChakra() { return chakra; }
    @Override public void setChakra(float chakra) { this.chakra = chakra; }
    @Override public int getSharinganStage() { return sharinganStage; }
    @Override public void setSharinganStage(int stage) { this.sharinganStage = stage; }
    @Override public boolean isInKamuiDimension() { return inKamui; }
    @Override public void setInKamuiDimension(boolean inDimension) { this.inKamui = inDimension; }

    // NEW: Getters and Setters
    @Override public boolean isSharinganActive() { return sharinganActive; }
    @Override public void setSharinganActive(boolean active) { this.sharinganActive = active; }
    @Override public int getNinjutsuStat() { return ninjutsuStat; }
    @Override public void setNinjutsuStat(int level) { this.ninjutsuStat = level; }
    @Override public int getGenjutsuStat() { return genjutsuStat; }
    @Override public void setGenjutsuStat(int level) { this.genjutsuStat = level; }

    @Override
    public void copyFrom(IShinobiData source) {
        this.chakra = source.getChakra();
        this.sharinganActive = source.isSharinganActive();
        this.sharinganStage = source.getSharinganStage();
        this.inKamui = source.isInKamuiDimension();
        // Keep stats after death!
        this.ninjutsuStat = source.getNinjutsuStat();
        this.genjutsuStat = source.getGenjutsuStat();
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putFloat("chakra", chakra);
        compound.putBoolean("sharinganActive", sharinganActive);
        compound.putInt("sharinganStage", sharinganStage);
        compound.putBoolean("inKamui", inKamui);
        // Save to hard drive
        compound.putInt("ninjutsuStat", ninjutsuStat);
        compound.putInt("genjutsuStat", genjutsuStat);
    }

    public void loadNBTData(CompoundTag compound) {
        chakra = compound.getFloat("chakra");
        sharinganActive = compound.getBoolean("sharinganActive");
        sharinganStage = compound.getInt("sharinganStage");
        inKamui = compound.getBoolean("inKamui");
        // Load from hard drive
        ninjutsuStat = compound.getInt("ninjutsuStat");
        genjutsuStat = compound.getInt("genjutsuStat");
    }
}