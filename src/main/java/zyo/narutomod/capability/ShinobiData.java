package zyo.narutomod.capability;

import net.minecraft.nbt.CompoundTag;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Archetype;
import zyo.narutomod.player.Village;

import java.util.HashMap;
import java.util.Map;

public class ShinobiData implements IShinobiData {
    private float chakra = 100.0f;
    private int sharinganStage = 0;
    private String msVariant = "none";
    private boolean sharinganActive = false;
    private boolean inKamui = false;
    private boolean cloneInfusionReady = false;

    private int ninjutsuStat = 1;
    private int genjutsuStat = 1;

    private final Map<String, Integer> natureMastery = new HashMap<>();
    private final java.util.List<String> unlockedJutsus = new java.util.ArrayList<>();
    private Clan clan = Clan.CLANLESS;
    private Archetype archetype = Archetype.NONE;
    private Village village = Village.NONE;

    @Override public float getChakra() { return chakra; }
    @Override public void setChakra(float chakra) { this.chakra = chakra; }
    @Override public int getSharinganStage() { return sharinganStage; }
    @Override public void setSharinganStage(int stage) { this.sharinganStage = stage; }
    @Override public boolean isInKamuiDimension() { return inKamui; }
    @Override public void setInKamuiDimension(boolean inDimension) { this.inKamui = inDimension; }
    
    @Override public String getMsVariant() { return msVariant; }
    @Override public void setMsVariant(String variant) { this.msVariant = variant; }

    @Override public boolean isSharinganActive() { return sharinganActive; }
    @Override public void setSharinganActive(boolean active) { this.sharinganActive = active; }
    @Override public int getNinjutsuStat() { return ninjutsuStat; }
    @Override public void setNinjutsuStat(int level) { this.ninjutsuStat = level; }
    @Override public int getGenjutsuStat() { return genjutsuStat; }
    @Override public void setGenjutsuStat(int level) { this.genjutsuStat = level; }

    @Override public boolean isCloneInfusionReady() { return this.cloneInfusionReady; }
    @Override public void setCloneInfusionReady(boolean ready) { this.cloneInfusionReady = ready;}

    @Override public Clan getClan() { return clan; }
    @Override public void setClan(Clan clan) { this.clan = clan; }

    @Override public Archetype getArchetype() { return archetype; }
    @Override public void setArchetype(Archetype archetype) { this.archetype = archetype; }

    @Override public Village getVillage() { return village; }
    @Override public void setVillage(Village village) { this.village = village; }

    @Override public java.util.List<String> getUnlockedJutsus() { return this.unlockedJutsus; }

    @Override
    public void unlockJutsu(String jutsuId) {
        if (!this.unlockedJutsus.contains(jutsuId)) {
            this.unlockedJutsus.add(jutsuId);
        }
    }

    @Override
    public boolean hasJutsu(String jutsuId) {
        return this.unlockedJutsus.contains(jutsuId);
    }

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

        return baseMax + (this.ninjutsuStat * 15.0f);
    }

    @Override
    public int getNatureMastery(String nature) {
        if (nature == null) return 0;
        return natureMastery.getOrDefault(nature.toLowerCase(), 0);
    }

    @Override
    public void addNatureMastery(String nature, int amount) {
        if (nature == null) return;
        String key = nature.toLowerCase();
        natureMastery.put(key, getNatureMastery(key) + amount);
    }

    @Override
    public void copyFrom(IShinobiData source) {
        this.chakra = source.getChakra();
        this.sharinganActive = source.isSharinganActive();
        this.sharinganStage = source.getSharinganStage();
        this.msVariant = source.getMsVariant(); // Copy variant
        this.inKamui = source.isInKamuiDimension();
        this.ninjutsuStat = source.getNinjutsuStat();
        this.genjutsuStat = source.getGenjutsuStat();
        this.clan = source.getClan();
        this.archetype = source.getArchetype();
        this.village = source.getVillage();
        this.unlockedJutsus.clear();
        this.unlockedJutsus.addAll(source.getUnlockedJutsus());
        if (source instanceof ShinobiData sData) {
            this.natureMastery.clear();
            this.natureMastery.putAll(sData.natureMastery);
        }
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putFloat("chakra", chakra);
        compound.putBoolean("sharinganActive", sharinganActive);
        compound.putInt("sharinganStage", sharinganStage);
        compound.putString("msVariant", msVariant); // Save variant
        compound.putBoolean("inKamui", inKamui);
        compound.putInt("ninjutsuStat", ninjutsuStat);
        compound.putInt("genjutsuStat", genjutsuStat);
        compound.putBoolean("CloneInfusionReady", this.cloneInfusionReady);
        compound.putString("PlayerClan", clan.name());
        compound.putString("PlayerArchetype", archetype.name());
        compound.putString("PlayerVillage", village.name());

        net.minecraft.nbt.ListTag jutsuList = new net.minecraft.nbt.ListTag();
        for (String jutsu : unlockedJutsus) {
            jutsuList.add(net.minecraft.nbt.StringTag.valueOf(jutsu));
        }
        compound.put("UnlockedJutsus", jutsuList);

        CompoundTag masteryTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : natureMastery.entrySet()) {
            masteryTag.putInt(entry.getKey(), entry.getValue());
        }
        compound.put("NatureMastery", masteryTag);
    }

    public void loadNBTData(CompoundTag compound) {
        chakra = compound.getFloat("chakra");
        sharinganActive = compound.getBoolean("sharinganActive");
        sharinganStage = compound.getInt("sharinganStage");
        if (compound.contains("msVariant")) {
            msVariant = compound.getString("msVariant");
        }
        inKamui = compound.getBoolean("inKamui");
        ninjutsuStat = compound.getInt("ninjutsuStat");
        genjutsuStat = compound.getInt("genjutsuStat");
        cloneInfusionReady = compound.getBoolean("CloneInfusionReady");

        if (compound.contains("PlayerClan")) {
            try {
                clan = Clan.valueOf(compound.getString("PlayerClan"));
            } catch (IllegalArgumentException e) {
                clan = Clan.CLANLESS;
            }
        }

        if (compound.contains("PlayerArchetype")) {
            try {
                archetype = Archetype.valueOf(compound.getString("PlayerArchetype"));
            } catch (IllegalArgumentException e) {
                archetype = Archetype.NONE;
            }
        }

        if (compound.contains("PlayerVillage")) {
            try {
                village = Village.valueOf(compound.getString("PlayerVillage"));
            } catch (IllegalArgumentException e) {
                village = Village.NONE;
            }
        }

        this.unlockedJutsus.clear();
        if (compound.contains("UnlockedJutsus")) {
            net.minecraft.nbt.ListTag jutsuList = compound.getList("UnlockedJutsus", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < jutsuList.size(); i++) {
                this.unlockedJutsus.add(jutsuList.getString(i));
            }
        }

        this.natureMastery.clear();
        if (compound.contains("NatureMastery")) {
            CompoundTag masteryTag = compound.getCompound("NatureMastery");
            for (String key : masteryTag.getAllKeys()) {
                this.natureMastery.put(key, masteryTag.getInt(key));
            }
        }
    }
}