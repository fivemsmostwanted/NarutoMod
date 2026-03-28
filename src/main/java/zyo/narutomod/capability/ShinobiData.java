package zyo.narutomod.capability;

import net.minecraft.nbt.CompoundTag;

public class ShinobiData implements IShinobiData {
    private float chakra = 100.0f;
    private int sharinganStage = 0;
    private boolean sharinganActive = false;
    private boolean inKamui = false;
    private boolean cloneInfusionReady = false;

    private int ninjutsuStat = 1;
    private int genjutsuStat = 1;

    private UchihaArchetype archetype = UchihaArchetype.NONE;

    @Override public float getChakra() { return chakra; } 
    @Override public void setChakra(float chakra) { this.chakra = chakra; } 
    @Override public int getSharinganStage() { return sharinganStage; } 
    @Override public void setSharinganStage(int stage) { this.sharinganStage = stage; } 
    @Override public boolean isInKamuiDimension() { return inKamui; } 
    @Override public void setInKamuiDimension(boolean inDimension) { this.inKamui = inDimension; } 

    @Override public boolean isSharinganActive() { return sharinganActive; } 
    @Override public void setSharinganActive(boolean active) { this.sharinganActive = active; } 
    @Override public int getNinjutsuStat() { return ninjutsuStat; } 
    @Override public void setNinjutsuStat(int level) { this.ninjutsuStat = level; } 
    @Override public int getGenjutsuStat() { return genjutsuStat; } 
    @Override public void setGenjutsuStat(int level) { this.genjutsuStat = level; } 

    @Override public boolean isCloneInfusionReady() { return this.cloneInfusionReady; }
    @Override public void setCloneInfusionReady(boolean ready) { this.cloneInfusionReady = ready;}

    @Override public UchihaArchetype getArchetype() { return archetype; }
    @Override public void setArchetype(UchihaArchetype archetype) { this.archetype = archetype; }

    @Override
    public float getMaxChakra() {
        float baseMax = switch (this.sharinganStage) {
            case 1 -> 100.0f;
            case 2 -> 250.0f;
            case 3 -> 500.0f;
            case 4 -> 750.0f;
            case 5 -> 1000.0f;
            case 6 -> 1500.0f;
            default -> 100.0f;
        };

        // give +15 max chakra
        return baseMax + (this.ninjutsuStat * 15.0f);
    }

    @Override
    public void copyFrom(IShinobiData source) { 
        this.chakra = source.getChakra(); 
        this.sharinganActive = source.isSharinganActive(); 
        this.sharinganStage = source.getSharinganStage(); 
        this.inKamui = source.isInKamuiDimension(); 
        this.ninjutsuStat = source.getNinjutsuStat(); 
        this.genjutsuStat = source.getGenjutsuStat(); 
        this.archetype = source.getArchetype();
    }

    public void saveNBTData(CompoundTag compound) { 
        compound.putFloat("chakra", chakra); 
        compound.putBoolean("sharinganActive", sharinganActive); 
        compound.putInt("sharinganStage", sharinganStage); 
        compound.putBoolean("inKamui", inKamui); 
        compound.putInt("ninjutsuStat", ninjutsuStat); 
        compound.putInt("genjutsuStat", genjutsuStat); 
        compound.putString("uchihaArchetype", archetype.name());
        compound.putBoolean("CloneInfusionReady", this.cloneInfusionReady);
    }

    public void loadNBTData(CompoundTag compound) { 
        chakra = compound.getFloat("chakra"); 
        sharinganActive = compound.getBoolean("sharinganActive"); 
        sharinganStage = compound.getInt("sharinganStage"); 
        inKamui = compound.getBoolean("inKamui"); 
        ninjutsuStat = compound.getInt("ninjutsuStat"); 
        genjutsuStat = compound.getInt("genjutsuStat"); 
        cloneInfusionReady = compound.getBoolean("CloneInfusionReady");

        try {
            archetype = UchihaArchetype.valueOf(compound.getString("uchihaArchetype"));
        } catch (IllegalArgumentException e) {
            archetype = UchihaArchetype.NONE;
        }
    }
}