package com.delke.villagers.villagers.profession;

import com.delke.villagers.villagers.behavior.lumberjack.CutTree;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;

import java.util.List;

import static com.delke.villagers.villagers.VillagerManager.LUMBERJACK_POI;

/**
 * @author Bailey Delker
 * @created 07/22/2023 - 1:16 PM
 * @project Villagers-1.18.2
 */
public class LumberJack extends AbstractProfession {
    public LumberJack() {
        super(
                "lumberjack",
                LUMBERJACK_POI.get(),
                ImmutableSet.of(

                ),
                ImmutableSet.of(),
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD
        );
    }

    @Override
    public List<Pair<Behavior<? super Villager>, Integer>> getSecondWorkPackage() {
        return List.of(
                Pair.of(new CutTree(), 100)
        );
    }
}
