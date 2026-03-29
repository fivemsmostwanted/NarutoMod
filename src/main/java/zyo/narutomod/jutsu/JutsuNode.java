package zyo.narutomod.jutsu;

import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;

import java.util.ArrayList;
import java.util.List;

public class JutsuNode {
    private final ResourceLocation jutsuId;
    private final int gridX;
    private final int gridY;

    private JutsuNode parent = null;
    private final List<JutsuNode> children = new ArrayList<>();

    private Clan requiredClan = Clan.CLANLESS;
    private Village requiredVillage = Village.NONE;
    private int requiredNinjutsuLevel = 1;
    private int xpCost = 1;

    private int requiredSharinganStage = 0;
    private ResourceLocation customIcon = null;

    public JutsuNode setXpCost(int levels) {
        this.xpCost = levels;
        return this;
    }
    public int getXpCost() { return xpCost; }

    public JutsuNode(ResourceLocation jutsuId, int gridX, int gridY) {
        this.jutsuId = jutsuId;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public JutsuNode setParent(JutsuNode parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        return this;
    }

    public JutsuNode requireClan(Clan clan) {
        this.requiredClan = clan;
        return this;
    }

    public JutsuNode requireVillage(Village village) {
        this.requiredVillage = village;
        return this;
    }

    public JutsuNode requireLevel(int level) {
        this.requiredNinjutsuLevel = level;
        return this;
    }

    public JutsuNode requireSharingan(int stage) {
        this.requiredSharinganStage = stage;
        return this;
    }

    public JutsuNode setCustomIcon(ResourceLocation icon) {
        this.customIcon = icon;
        return this;
    }

    public int getRequiredSharinganStage() { return requiredSharinganStage; }
    public ResourceLocation getCustomIcon() { return customIcon; }
    public ResourceLocation getJutsuId() { return jutsuId; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public JutsuNode getParent() { return parent; }
    public List<JutsuNode> getChildren() { return children; }
    public Clan getRequiredClan() { return requiredClan; }
    public Village getRequiredVillage() { return requiredVillage; }
    public int getRequiredNinjutsuLevel() { return requiredNinjutsuLevel; }
}