package zyo.narutomod.jutsu;

import net.minecraft.resources.ResourceLocation;
import zyo.narutomod.player.Clan;
import java.util.*;

public class JutsuTreeManager {
    public static final Map<ResourceLocation, JutsuNode> ALL_NODES = new HashMap<>();
    public static final List<JutsuNode> ROOT_NODES = new ArrayList<>();

    public static void initializeTrees() {
        ALL_NODES.clear();
        ROOT_NODES.clear();

        JutsuManager.LOADED_JUTSUS.forEach((id, data) -> {
            Clan clan = data.required_clan != null ? Clan.valueOf(data.required_clan) : Clan.CLANLESS;

            JutsuNode node = new JutsuNode(
                    id,
                    data.grid_x,
                    data.grid_y,
                    data.xp_cost,
                    data.required_level,
                    data.required_sharingan,
                    clan,
                    data.custom_icon
            );
            ALL_NODES.put(id, node);
        });

        ALL_NODES.forEach((id, node) -> {
            JutsuData data = JutsuManager.LOADED_JUTSUS.get(id);
            ResourceLocation parentId = data.getParentId();

            if (parentId != null && ALL_NODES.containsKey(parentId)) {
                JutsuNode parent = ALL_NODES.get(parentId);
                node.setParent(parent);
                parent.addChild(node);
            } else {
                ROOT_NODES.add(node);
            }
        });
    }
}