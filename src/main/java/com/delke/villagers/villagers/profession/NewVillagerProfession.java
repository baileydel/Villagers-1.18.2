package com.delke.villagers.villagers.profession;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bailey Delker
 * @created 06/18/2023 - 12:21 PM
 * @project Villagers-1.18.2
 */
public abstract class NewVillagerProfession extends VillagerProfession {
    public NewVillagerProfession(String name, PoiType block, ImmutableSet<Item> request, ImmutableSet<Block> idk, @Nullable SoundEvent workSound) {
        super(name, block, request, idk, workSound);
    }

    abstract ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWork();

    abstract ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCore();

    abstract Schedule getSchedule();

    public abstract void registerBrain(Brain<Villager> brain);

    public abstract void registerGoals(Villager villager);
}
