package com.delke.villagers.villagers.profession;

import com.delke.villagers.villagers.VillagerManager;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class Builder extends AbstractProfession {
    public Builder() {
        super("builder",
                VillagerManager.BUILDER_POI.get(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_FARMER
        );
    }
}
