package com.delke.villagers.villagers.behavior;

import com.delke.villagers.villagers.VillagerManager;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

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

        this.predicate = (entity) -> EntityType.VILLAGER.equals(entity.getType());
        this.targetVillager = Optional.empty();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Villager villager) {

        // Set second villager memory
        if (villager.getBrain().getMemory(VillagerManager.TRADING_ENTITY.get()).isPresent()) {
            targetVillager = villager.getBrain().getMemory(VillagerManager.TRADING_ENTITY.get());
            return true;
        }

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
    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long currentTime) {

        // find a villager
        if (targetVillager.isEmpty()) {
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

        // First villager walks to other
        BehaviorUtils.setWalkAndLookTargetMemories(villager, targetVillager.get(), 0.5F, 2);

        // Initiate second villager to trade with 1st
        if (villager.distanceTo(targetVillager.get()) < 3 && targetVillager.get().getBrain().getMemory(VillagerManager.TRADING_ENTITY.get()).isEmpty()) {
            Brain<?> brain = villager.getBrain();
            Brain<?> target_brain = targetVillager.get().getBrain();

            target_brain.setMemory(VillagerManager.TRADING_ENTITY.get(), villager);
            target_brain.setMemory(VillagerManager.NEED_ITEM.get(), new ItemStack(Items.EMERALD));

            BehaviorUtils.lookAtEntity(targetVillager.get(), villager);

            if (targetVillager.get() instanceof Villager t_v) {
                ItemStack first_villager = ItemStack.EMPTY;
                ItemStack second_villager = ItemStack.EMPTY;

                ItemStack farmer = brain.getMemory(VillagerManager.NEED_ITEM.get()).get();
                ItemStack toolsmith = target_brain.getMemory(VillagerManager.NEED_ITEM.get()).get();

                int f = 0;
                int s = 0;

                //TODO Further itemstack checking, villager might need 2 emeralds, not just 1, might need certain nbt values.. ect
                // Find the requested item from our target villager

                SimpleContainer t_inv = t_v.getInventory();
                SimpleContainer inv = villager.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    // Farmer
                    if (inv.getItem(i).is(toolsmith.getItem())) {
                        first_villager = toolsmith;
                        s = i;
                    }

                    if (t_inv.getItem(i).is(farmer.getItem())) {
                        second_villager = farmer;
                        f = i;
                    }
                }

                inv.removeItem(f, 1);
                inv.addItem(second_villager);

                t_inv.removeItem(s, 1);
                t_inv.addItem(first_villager);

                brain.eraseMemory(VillagerManager.NEED_ITEM.get());
                brain.eraseMemory(VillagerManager.TRADING_ENTITY.get());

                target_brain.eraseMemory(VillagerManager.NEED_ITEM.get());
                target_brain.eraseMemory(VillagerManager.TRADING_ENTITY.get());
            }
        }
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel p_22545_, @NotNull Villager p_22546_, long p_22547_) {
        return true;
    }
}
