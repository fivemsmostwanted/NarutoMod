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
    private float permanentChakraBonus = 0.0f;
    private String msVariant = "none";
    private boolean sharinganActive = false;
    private boolean inKamui = false;
    private boolean cloneInfusionReady = false;

    private int ninjutsuStat = 1;
    private int genjutsuStat = 1;

    private final Map<String, Integer> natureMastery = new HashMap<>();
    private final java.util.List<String> unlockedJutsus = new java.util.ArrayList<>();
    private final Map<String, Integer> activeCooldowns = new HashMap<>();
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

    @Override public Map<String, Integer> getActiveCooldowns() { return this.activeCooldowns; }

    private int msBleedTimer = 0;

    @Override public int getMsBleedTimer() { return msBleedTimer; }
    @Override public void setMsBleedTimer(int ticks) { this.msBleedTimer = ticks; }

    @Override public java.util.List<String> getUnlockedJutsus() { return this.unlockedJutsus; }

    private int experience = 0;
    @Override public int getExperience() { return this.experience; }
    @Override public void setExperience(int xp) { this.experience = xp; }

    @Override
    public void setCooldown(String jutsuId, int ticks) {
        this.activeCooldowns.put(jutsuId, ticks);
    }

    @Override
    public boolean isOnCooldown(String jutsuId) {
        return this.activeCooldowns.containsKey(jutsuId) && this.activeCooldowns.get(jutsuId) > 0;
    }

    @Override
    public void tickCooldowns() {
        this.activeCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }

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
        return 100.0f + this.permanentChakraBonus + (this.ninjutsuStat * 15.0f);
    }

    public void addPermanentChakra(float amount) {
        this.permanentChakraBonus += amount;
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
        this.msVariant = source.getMsVariant();
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
            this.activeCooldowns.clear();
            this.activeCooldowns.putAll(sData.activeCooldowns);
        }
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putFloat("chakra", chakra);
        compound.putBoolean("sharinganActive", sharinganActive);
        compound.putInt("sharinganStage", sharinganStage);
        compound.putString("msVariant", msVariant);
        compound.putBoolean("inKamui", inKamui);
        compound.putInt("ninjutsuStat", ninjutsuStat);
        compound.putInt("genjutsuStat", genjutsuStat);
        compound.putBoolean("CloneInfusionReady", this.cloneInfusionReady);
        compound.putString("PlayerClan", clan.name());
        compound.putString("PlayerArchetype", archetype.name());
        compound.putString("PlayerVillage", village.name());
        compound.putInt("ShinobiExperience", this.experience);
        compound.putFloat("PermanentChakraBonus", this.permanentChakraBonus);

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

        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : activeCooldowns.entrySet()) {
            cooldownTag.putInt(entry.getKey(), entry.getValue());
        }
        compound.put("ActiveCooldowns", cooldownTag);
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
        permanentChakraBonus = compound.getFloat("PermanentChakraBonus");

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

        this.activeCooldowns.clear();
        if (compound.contains("ActiveCooldowns")) {
            CompoundTag cooldownTag = compound.getCompound("ActiveCooldowns");
            for (String key : cooldownTag.getAllKeys()) {
                this.activeCooldowns.put(key, cooldownTag.getInt(key));
            }
        }

        if (compound.contains("ShinobiExperience")) {
            this.experience = compound.getInt("ShinobiExperience");
        }
    }
}