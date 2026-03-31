package zyo.narutomod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import zyo.narutomod.NarutoMod;
import zyo.narutomod.client.model.AkatsukiCloakModel;

import java.util.function.Consumer;

public class AkatsukiCloakItem extends ArmorItem {

    public AkatsukiCloakItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AkatsukiCloakModel<?> model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                if (this.model == null) {
                    this.model = new AkatsukiCloakModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AkatsukiCloakModel.LAYER_LOCATION));
                }
                return this.model;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return NarutoMod.MODID + ":textures/models/armor/akatsuki_cloak.png";
    }
}