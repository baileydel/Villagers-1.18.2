package com.delke.villagers.villagers.behavior;

import com.delke.villagers.villagers.VillagerManager;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Bailey Delker
 * @created 07/13/2023 - 5:18 AM
 * @project Villagers-1.18.2
 */
public class TradeForItem extends Behavior<Villager> {

    private final Predicate<LivingEntity> predicate;

    private Optional<LivingEntity> targetVillager;

    public TradeForItem() {
        super(ImmutableMap.of(
                VillagerManager.NEED_ITEM.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));

        this.predicate = (entity) -> EntityType.VILLAGER.equals(entity.getType());;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        // get villager needed item to continue to work, or such
        ItemStack need = villager.getBrain().getMemory(VillagerManager.NEED_ITEM.get()).get();

        // Check if we can produce the item ourselves
        for (ItemStack item : ((AbstractProfession) villager.getVillagerData().getProfession()).getProducibleItems()) {
            if (item.is(need.getItem())) {
                // Return false if we can produce it
                return false;
            }
        }

        // Start trying to obtain the item
        return true;
    }

    @Override
    protected void start(ServerLevel level, Villager villager, long currentTime) {
        // Find the villager that has the item

        NearestVisibleLivingEntities nearestvisiblelivingentities = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        targetVillager = nearestvisiblelivingentities.findClosest(
                this.predicate.and(
                        (entity) -> {
                            if (entity instanceof Villager targetVillager) {
                                return targetVillager.getVillagerData().getProfession().getName().equals("newtoolsmith");
                            }
                            return false;
                        })
        );
    }

    @Override
    protected void tick(ServerLevel p_22551_, Villager villager, long p_22553_) {
        // Constantly set our walk and look target, otherwise other behaviors might stop our entry values.
        // resulting in the behavior ending earlier than we want.
        BehaviorUtils.setWalkAndLookTargetMemories(villager, targetVillager.get(), 0.5F, 2);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, Villager p_22546_, long p_22547_) {
        return true;
    }
}
