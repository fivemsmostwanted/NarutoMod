package zyo.narutomod.jutsu;

import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.player.Clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JutsuTreeManager {
    public static final List<JutsuNode> ROOT_NODES = new ArrayList<>();
    public static final Map<ResourceLocation, JutsuNode> ALL_NODES = new HashMap<>();

    public static void initializeTrees() {
        ROOT_NODES.clear();
        ALL_NODES.clear();

        JutsuNode substitution = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "substitution"), 0, -2)
                .setXpCost(2)
                .requireLevel(1)
                .setCustomIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/gui/jutsus/jutsu_substiutution_marker.png"))
                .setLockedIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/gui/jutsus/jutsu_substiutution.png"));
        ROOT_NODES.add(substitution);
        ALL_NODES.put(substitution.getJutsuId(), substitution);

        JutsuNode cloneJutsu = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "crow_clone"), 1, -2)
                .setParent(substitution)
                .setXpCost(2)
                .requireLevel(2);
        ALL_NODES.put(cloneJutsu.getJutsuId(), cloneJutsu);

        JutsuNode fireball = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "fireball"), 0, 0)
                .requireClan(Clan.UCHIHA)
                .setXpCost(3)
                .requireLevel(1);
        ROOT_NODES.add(fireball);
        ALL_NODES.put(fireball.getJutsuId(), fireball);

        JutsuNode phoenixFlower = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "phoenix_flower_jutsu"), 1, 0)
                .setParent(fireball)
                .requireClan(Clan.UCHIHA)
                .setXpCost(5)
                .requireLevel(3);
        ALL_NODES.put(phoenixFlower.getJutsuId(), phoenixFlower);

        JutsuNode sharinganRoot = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "sharingan_root"), 0, 2)
                .requireClan(Clan.UCHIHA)
                .requireSharingan(0)
                .setXpCost(999)
                .setCustomIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/hud/sharingan_1.png"))
                .setLockedIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/hud/sharingan_locked.png"));
        ROOT_NODES.add(sharinganRoot);
        ALL_NODES.put(sharinganRoot.getJutsuId(), sharinganRoot);

        JutsuNode sharingan2 = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "sharingan_2"), 1, 2)
                .setParent(sharinganRoot)
                .requireClan(Clan.UCHIHA)
                .requireSharingan(1)
                .setXpCost(10)
                .setCustomIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/hud/sharingan_2.png"));
        ALL_NODES.put(sharingan2.getJutsuId(), sharingan2);

        JutsuNode sharingan3 = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "sharingan_3"), 2, 2)
                .setParent(sharingan2)
                .requireClan(Clan.UCHIHA)
                .requireSharingan(2)
                .setXpCost(15)
                .setCustomIcon(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "textures/hud/sharingan_3.png"));
        ALL_NODES.put(sharingan3.getJutsuId(), sharingan3);

        JutsuNode crowClone = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "crow_clone_feint"), 3, 1)
                .setParent(sharingan3)
                .requireClan(Clan.UCHIHA)
                .setXpCost(5);
        ALL_NODES.put(crowClone.getJutsuId(), crowClone);

        JutsuNode shacklingStakes = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "shackling_stakes"), 3, 2)
                .setParent(sharingan3)
                .requireClan(Clan.UCHIHA)
                .setXpCost(12);
        ALL_NODES.put(shacklingStakes.getJutsuId(), shacklingStakes);

        JutsuNode tsukuyomi = new JutsuNode(ResourceLocation.fromNamespaceAndPath(NarutoMod.MODID, "tsukuyomi"), 3, 3)
                .setParent(sharingan3)
                .requireClan(Clan.UCHIHA)
                .setXpCost(20);
        ALL_NODES.put(tsukuyomi.getJutsuId(), tsukuyomi);
    }
}