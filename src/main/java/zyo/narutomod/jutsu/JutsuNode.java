package zyo.narutomod.jutsu;

import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.player.Clan;
import zyo.narutomod.player.Village;
import java.util.ArrayList;
import java.util.List;

public class JutsuNode {
    private final ResourceLocation jutsuId;
    private JutsuNode parent;
    private final List<JutsuNode> children = new ArrayList<>();
    private final int gridX, gridY, xpCost, reqNinjutsu, reqSharingan;
    private final Clan reqClan;
    private final ResourceLocation customIcon;

    public JutsuNode(ResourceLocation id, int x, int y, int xp, int nin, int sharingan, Clan clan, String icon) {
        this.jutsuId = id;
        this.gridX = x;
        this.gridY = y;
        this.xpCost = xp;
        this.reqNinjutsu = nin;
        this.reqSharingan = sharingan;
        this.reqClan = clan;
        this.customIcon = icon != null ? ResourceLocation.parse(icon) : null;
    }

    public void setParent(JutsuNode p) { this.parent = p; }
    public void addChild(JutsuNode c) { this.children.add(c); }

    public ResourceLocation getJutsuId() { return jutsuId; }
    public JutsuNode getParent() { return parent; }
    public List<JutsuNode> getChildren() { return children; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public int getXpCost() { return xpCost; }
    public int getRequiredNinjutsuLevel() { return reqNinjutsu; }
    public int getRequiredGenjutsuLevel() { return 0; }
    public int getRequiredSharinganStage() { return reqSharingan; }
    public Clan getRequiredClan() { return reqClan; }
    public Village getRequiredVillage() { return Village.NONE; }

    public ResourceLocation getCustomIcon() {
        if (customIcon != null) return customIcon;

        String path = jutsuId.getPath();
        if (path.contains("sharingan") || path.contains("mangekyou") || path.contains("rinnegan")) {
            return ResourceLocation.fromNamespaceAndPath(jutsuId.getNamespace(), "textures/hud/" + path + ".png");
        }

        return ResourceLocation.fromNamespaceAndPath(jutsuId.getNamespace(), "textures/gui/jutsus/" + path + ".png");
    }

    public ResourceLocation getLockedIcon() { return null; }
}