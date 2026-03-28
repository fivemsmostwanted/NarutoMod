package zyo.narutomod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShinobiDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    // key for access
    public static Capability<ShinobiData> SHINOBI_DATA = CapabilityManager.get(new CapabilityToken<ShinobiData>() { });

    private ShinobiData shinobiData = null;
    private final LazyOptional<ShinobiData> optional = LazyOptional.of(this::createShinobiData);

    private ShinobiData createShinobiData() {
        if (this.shinobiData == null) {
            this.shinobiData = new ShinobiData();
        }
        return this.shinobiData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SHINOBI_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createShinobiData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createShinobiData().loadNBTData(nbt);
    }
}