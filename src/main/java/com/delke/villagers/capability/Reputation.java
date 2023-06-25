package com.delke.villagers.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * @author Bailey Delker
 * @created 06/24/2023 - 1:34 PM
 * @project Villagers-1.18.2
 */
public class Reputation {
    private int reputation;

    public Reputation(int reputation) {
        this.reputation = reputation;
    }

    public int getReputation() {
        return this.reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void addReputation(int reputation) {
        this.reputation += reputation;
    }

    public void copyFrom(Reputation source) {
        this.reputation = source.reputation;
    }

    public void saveNBT(CompoundTag compoundTag) {
        compoundTag.putInt("reputation", reputation);
    }

    public void loadNBT(CompoundTag compoundTag) {
        this.reputation = compoundTag.getInt("reputation");
    }
}
