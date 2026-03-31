package zyo.narutomod.jutsu;

import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class JutsuData {
    public int id;
    public String type;
    public String name;
    public float chakra_cost;
    public int xp_cost;
    public int cooldown;
    public List<Integer> hand_signs;
    public String nature;

    // Skill Tree Fields
    public String parent;
    public int grid_x;
    public int grid_y;
    public int required_level;
    public String required_clan;
    public int required_sharingan;
    public String custom_icon;

    public ResourceLocation getParentId() {
        return parent != null ? ResourceLocation.parse(parent) : null;
    }
}