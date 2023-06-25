package com.delke.villagers.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bailey Delker
 * @created 06/24/2023 - 1:32 PM
 * @project Villagers-1.18.2
 */
public class ReputationProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<Reputation> PLAYER_REPUTATION = CapabilityManager.get(new CapabilityToken<>() {});
    private Reputation playerReputation = null;
    private final LazyOptional<Reputation> optional = LazyOptional.of(this::createPlayerReputation);

    private Reputation createPlayerReputation() {
        if (playerReputation == null) {
            playerReputation = new Reputation(0);
        }
        return playerReputation;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == PLAYER_REPUTATION) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createPlayerReputation().saveNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerReputation().loadNBT(nbt);
    }
}
