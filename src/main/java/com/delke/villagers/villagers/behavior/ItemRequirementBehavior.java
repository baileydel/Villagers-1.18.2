package com.delke.villagers.villagers.behavior;

import com.delke.villagers.villagers.VillagerUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

//make the villager need an item in order to do their job
//use for every villager
public class ItemRequirementBehavior extends Behavior<Villager> {

    private final ItemStack stack;
    public ItemRequirementBehavior(Map<MemoryModuleType<?>, MemoryStatus> p_22528_, ItemStack stack) {
        super(p_22528_);
        this.stack = stack;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
        return VillagerUtil.hasEnoughOf(villager, stack);
    }

    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long p_23206_) {
        return VillagerUtil.hasEnoughOf(villager, stack);
    }
}
