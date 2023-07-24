package com.delke.villagers.villagers.profession.override;

import com.delke.villagers.villagers.behavior.shepherd.ShearSheep;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/05/2023 - 8:49 PM
 * @project Villagers-1.18.2
 */
public class NewShepherd extends AbstractProfession {

    public NewShepherd() {
        super("newshepherd",
                PoiType.SHEPHERD,
                ImmutableSet.of(Items.SHEARS, Items.WHITE_WOOL),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_SHEPHERD
        );
    }

    @Override
    public List<Pair<Behavior<? super Villager>, Integer>> getSecondWorkPackage() {
        return ImmutableList.of(
                Pair.of(new ShearSheep(), 1)
        );
    }
}
