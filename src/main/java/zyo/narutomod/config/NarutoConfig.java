package zyo.narutomod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class NarutoConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<SelectionMode> SELECTION_MODE;

    public enum SelectionMode {
        MENU_CHOICE,
        RANDOM_ITEM
    }

    static {
        BUILDER.push("General Settings");

        SELECTION_MODE = BUILDER.comment("How players receive their starting Clan and Village.",
                        "MENU_CHOICE: Players pick exactly what they want via a GUI when they first join.",
                        "RANDOM_ITEM: Players are given a Shinobi Registration item to roll a random Clan/Village.")
                .defineEnum("selectionMode", SelectionMode.MENU_CHOICE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}