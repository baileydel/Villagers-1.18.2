package com.delke.villagers.villagers.behavior.lumberjack;

import com.delke.villagers.villagers.VillagerManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/25/2023 - 10:07 PM
 * @project Villagers-1.18.2
 */
public class CutTree extends Behavior<Villager> {

    private GlobalPos pos;

    public CutTree() {
        super(
                ImmutableMap.of(
                        VillagerManager.TODO.get(), MemoryStatus.VALUE_PRESENT
                ),
                500, 5000);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
        List<GlobalPos> list = villager.getBrain().getMemory(VillagerManager.TODO.get()).get();
        return list.size() > 0;
    }

    @Override
    protected void start(@NotNull ServerLevel level, Villager villager, long time) {
        List<GlobalPos> list = villager.getBrain().getMemory(VillagerManager.TODO.get()).get();

        pos = list.get(0);

        BehaviorUtils.setWalkAndLookTargetMemories(villager, pos.pos(), 0.5F, 1);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        if (pos.pos().closerToCenterThan(villager.position(), 3.3D)) {
            if (villager.getBrain().getMemory(VillagerManager.TODO.get()).isPresent()) {
                List<GlobalPos> list = villager.getBrain().getMemory(VillagerManager.TODO.get()).get();
                BlockState state = level.getBlockState(pos.pos());

                if (isValid(state)) {
                    findNext(level, pos.pos());
                }
                list.remove(pos);
                villager.getBrain().setMemory(VillagerManager.TODO.get(), list);
            }
        }
        else {
            BehaviorUtils.setWalkAndLookTargetMemories(villager, pos.pos(), 0.5F, 1);
        }
    }

    private void findNext(ServerLevel level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        BlockState north = level.getBlockState(pos.north());
        BlockState south = level.getBlockState(pos.south());
        BlockState east = level.getBlockState(pos.east());
        BlockState west = level.getBlockState(pos.west());

        level.destroyBlock(pos, true);

        if (isValid(above)) {
            findNext(level, pos.above());
        }

        if (isValid(below)) {
            findNext(level, pos.below());
        }

        if (isValid(north)) {
            findNext(level, pos.north());
        }

        if (isValid(south)) {
            findNext(level, pos.south());
        }

        if (isValid(east)) {
            findNext(level, pos.east());
        }

        if (isValid(west)) {
            findNext(level, pos.west());
        }
    }

    private boolean isValid(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        return isValid(level.getBlockState(pos.pos()));
    }
}
