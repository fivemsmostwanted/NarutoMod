package zyo.narutomod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import zyo.narutomod.jutsu.JutsuData;
import zyo.narutomod.jutsu.JutsuManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Supplier;

public class SyncJutsuRegistryPacket {
    private final Map<ResourceLocation, JutsuData> jutsus;

    public SyncJutsuRegistryPacket(Map<ResourceLocation, JutsuData> jutsus) {
        this.jutsus = jutsus;
    }

    // --- DECODER (Receiving the packet on the Client) ---
    public SyncJutsuRegistryPacket(FriendlyByteBuf buf) {
        this.jutsus = new HashMap<>();
        int size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            JutsuData data = new JutsuData();

            // Core Data
            data.id = buf.readInt();

            String type = buf.readUtf();
            data.type = type.isEmpty() ? null : type;

            String name = buf.readUtf();
            data.name = name.isEmpty() ? null : name;

            data.chakra_cost = buf.readFloat();
            data.xp_cost = buf.readInt();
            data.cooldown = buf.readInt();

            String nature = buf.readUtf();
            data.nature = nature.isEmpty() ? null : nature;

            // Skill Tree Data
            String parent = buf.readUtf();
            data.parent = parent.isEmpty() ? null : parent;

            data.grid_x = buf.readInt();
            data.grid_y = buf.readInt();
            data.required_level = buf.readInt();

            String required_clan = buf.readUtf();
            data.required_clan = required_clan.isEmpty() ? null : required_clan;

            data.required_sharingan = buf.readInt();

            String custom_icon = buf.readUtf();
            data.custom_icon = custom_icon.isEmpty() ? null : custom_icon;

            // Hand Signs Array
            int signCount = buf.readVarInt();
            if (signCount > 0) {
                data.hand_signs = new ArrayList<>();
                for (int j = 0; j < signCount; j++) {
                    data.hand_signs.add(buf.readVarInt());
                }
            } else {
                data.hand_signs = null;
            }

            this.jutsus.put(id, data);
        }
    }

    // --- ENCODER (Sending the packet from the Server) ---
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.jutsus.size());

        for (Map.Entry<ResourceLocation, JutsuData> entry : this.jutsus.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            JutsuData data = entry.getValue();

            // Core Data
            buf.writeInt(data.id);
            buf.writeUtf(data.type != null ? data.type : "");
            buf.writeUtf(data.name != null ? data.name : "");
            buf.writeFloat(data.chakra_cost);
            buf.writeInt(data.xp_cost);
            buf.writeInt(data.cooldown);
            buf.writeUtf(data.nature != null ? data.nature : "");

            // Skill Tree Data
            buf.writeUtf(data.parent != null ? data.parent : "");
            buf.writeInt(data.grid_x);
            buf.writeInt(data.grid_y);
            buf.writeInt(data.required_level);
            buf.writeUtf(data.required_clan != null ? data.required_clan : "");
            buf.writeInt(data.required_sharingan);
            buf.writeUtf(data.custom_icon != null ? data.custom_icon : "");

            // Hand Signs Array
            if (data.hand_signs != null && !data.hand_signs.isEmpty()) {
                buf.writeVarInt(data.hand_signs.size());
                for (int sign : data.hand_signs) {
                    buf.writeVarInt(sign);
                }
            } else {
                buf.writeVarInt(0);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                JutsuManager.LOADED_JUTSUS.clear();
                JutsuManager.LOADED_JUTSUS.putAll(this.jutsus);
                zyo.narutomod.jutsu.JutsuTreeManager.initializeTrees();
            });
        });
        context.setPacketHandled(true);
    }
}