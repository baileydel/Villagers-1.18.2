package com.delke.villagers.villagers.profession;

import com.delke.villagers.villagers.VillagerUtil;
import com.delke.villagers.villagers.behavior.lumberjack.CutTree;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.delke.villagers.villagers.VillagerManager.LUMBERJACK_POI;

/**
 * @author Bailey Delker
 * @created 07/22/2023 - 1:16 PM
 * @project Villagers-1.18.2
 */

/* TODO: 8/23/23
 * add level system, increases speed of tree cutting
 * cut trees one log at a time -speed based on type of axe
 * player can control the village and can determine what and where to build buildings
 * trades will change based on what items villagers have
 * add trades to villagers
 * behavior system
 */
public class LumberJack extends AbstractProfession {
    public LumberJack() {
        super(
                "lumberjack",
                LUMBERJACK_POI.get(),
                ImmutableSet.of(),
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD
        );
    }

    @Override
    public @NotNull ImmutableSet<Item> getRequestedItems() {
        List<Item> items = new ArrayList<>(VillagerUtil.itemPickup(List.of(ItemTags.LOGS, ItemTags.SAPLINGS)));
        items.add(Items.APPLE);
        items.add(Items.STICK);

        return ImmutableSet.copyOf(items);
    }

    @Override
    public List<Pair<Behavior<? super Villager>, Integer>> getSecondWorkPackage() {
        return List.of(
                Pair.of(new CutTree(), 1)
        );
    }
}
