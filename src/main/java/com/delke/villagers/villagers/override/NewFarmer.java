package com.delke.villagers.villagers.override;

import com.delke.villagers.villagers.behavior.TillFarmland;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * @author Bailey Delker
 * @created 06/27/2023 - 1:32 PM
 * @project Villagers-1.18.2
 */
public class NewFarmer extends AbstractProfession {
    public NewFarmer() {
        super("newfarmer",
                PoiType.FARMER,
                ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL),
                ImmutableSet.of(Blocks.FARMLAND),
                SoundEvents.VILLAGER_WORK_FARMER
        );
    }

    @Override
    public List<Pair<Behavior<? super Villager>, Integer>> getSecondWorkPackage() {
        return ImmutableList.of(
                Pair.of(new TillFarmland(), 1),
                Pair.of(new HarvestFarmland(), 2),
                Pair.of(new UseBonemeal(), 4)
        );
    }

    @Override
    public ImmutableList<Item> getProducibleItems() {
        return ImmutableList.of(
                Items.BREAD,
                Items.WOODEN_HOE
        );
    }

    @Override
    public boolean isProducer() {
        return true;
    }
}
