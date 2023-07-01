package com.delke.villagers.villagers.behavior;

import com.delke.villagers.capability.ReputationProvider;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author Bailey Delker
 * @created 06/24/2023 - 11:14 AM
 * @project Villagers-1.18.2
 */
public class ReactToReputation extends Behavior<Villager> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;
    private Optional<LivingEntity> nearestEntityMatchingTest = Optional.empty();

    public ReactToReputation(EntityType<?> p_23894_, float p_23895_) {
        this((p_23911_) -> p_23894_.equals(p_23911_.getType()), p_23895_);
    }

    public ReactToReputation(float p_23892_) {
        this((p_23913_) -> true, p_23892_);
    }

    public ReactToReputation(Predicate<LivingEntity> p_23900_, float p_23901_) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.predicate = p_23900_;
        this.maxDistSqr = p_23901_ * p_23901_;
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Villager villager) {
        NearestVisibleLivingEntities nearestvisiblelivingentities = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        this.nearestEntityMatchingTest = nearestvisiblelivingentities.findClosest(this.predicate.and(
                (p_186053_) -> p_186053_.distanceToSqr(villager) <= (double)this.maxDistSqr));
        return this.nearestEntityMatchingTest.isPresent();
    }

    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long p_23908_) {
        if (nearestEntityMatchingTest.isPresent() && nearestEntityMatchingTest.get() instanceof ServerPlayer player) {
            //TODO this is for normal villagers, guards will attack.
            player.getCapability(ReputationProvider.PLAYER_REPUTATION).ifPresent(reputation -> {

                /* Brain<?> brain = villager.getBrain();
                if (!brain.isActive(Activity.PANIC)) {
                    brain.eraseMemory(MemoryModuleType.PATH);
                    brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                    brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
                    brain.eraseMemory(MemoryModuleType.BREED_TARGET);
                    brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
                }

                brain.setMemory(MemoryModuleType.NEAREST_HOSTILE, player);
                brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, player);

                brain.setActiveActivityIfPossible(Activity.PANIC);

                player.sendMessage(new TextComponent("omg im panicking" + playerReputation.getReputation()), UUID.randomUUID());
                 */

            });
        }
        this.nearestEntityMatchingTest = Optional.empty();
    }
}
