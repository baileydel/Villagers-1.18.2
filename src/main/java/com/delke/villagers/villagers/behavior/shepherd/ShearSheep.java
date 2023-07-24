package com.delke.villagers.villagers.behavior.shepherd;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Bailey Delker
 * @created 07/05/2023 - 10:30 PM
 * @project Villagers-1.18.2
 */
public class ShearSheep extends Behavior<Villager> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;

    public ShearSheep() {
        this((entity) -> EntityType.SHEEP.equals(entity.getType()), 16);
    }

    public ShearSheep(Predicate<LivingEntity> predicate, float dist) {
        super(
                ImmutableMap.of(
                        MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
                )
        );
        this.predicate = predicate;
        this.maxDistSqr = dist * dist;
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Villager villager) {
        NearestVisibleLivingEntities nearestvisiblelivingentities = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        Optional<LivingEntity> nearestEntityMatchingTest = nearestvisiblelivingentities.findClosest(
                this.predicate.and(
                        (entity) -> {
                            if (entity instanceof Sheep sheep) {
                                if (!sheep.isSheared() && sheep.distanceToSqr(villager) <= (double) this.maxDistSqr) {
                                    villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, sheep);
                                    return true;
                                }
                            }
                            return false;
                        })
        );
        return nearestEntityMatchingTest.isPresent();
    }

    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long p_23908_) {
        BehaviorUtils.setWalkAndLookTargetMemories(villager, villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get(), 0.5F, 2);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, Villager villager, long p_22553_) {
        if (villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get() instanceof Sheep sheep) {
            if (!sheep.isSheared() && sheep.distanceToSqr(villager) <= 4) {
                sheep.shear(SoundSource.PLAYERS);
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, Villager villager, long p_22550_) {
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, Villager villager, long p_22547_) {
        if (villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get() instanceof Sheep sheep) {
            return !sheep.isSheared();
        }
        return false;
    }
}
