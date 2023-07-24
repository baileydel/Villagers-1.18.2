package com.delke.villagers.villagers.behavior;

import com.delke.villagers.villagers.VillagerManager;
import com.delke.villagers.villagers.VillagerUtil;
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
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Bailey Delker
 * @created 07/13/2023 - 5:18 AM
 * @project Villagers-1.18.2
 */
public class TradeForItem extends Behavior<Villager> {

    private final Predicate<LivingEntity> predicate;

    private Optional<LivingEntity> targetVillager;
    private boolean tradeDone;

    public TradeForItem() {
        super(ImmutableMap.of(
                VillagerManager.NEED_ITEM.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));

        this.predicate = (entity) -> EntityType.VILLAGER.equals(entity.getType());
        this.targetVillager = Optional.empty();
        this.tradeDone = false;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Villager villager) {
        this.tradeDone = false;
        Brain<Villager> brain = villager.getBrain();

        // This only is true when another villager is setting the memory of a villager it is trying to trade with.
        if (brain.getMemory(VillagerManager.TRADING_ENTITY.get()).isPresent()) {
            targetVillager = brain.getMemory(VillagerManager.TRADING_ENTITY.get());
            return true;
        }

        //TODO see if they are able to produce the items first,
        // see if they can currently produce it,
        if (targetVillager.isEmpty()) {
            NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();

            targetVillager = nearestvisiblelivingentities.findClosest(predicate.and(
                    (entity) -> {
                        if (brain.getMemory(VillagerManager.NEED_ITEM.get()).isPresent()) {

                            if (entity instanceof Villager targetVillager) {
                                ItemStack requestedItem = brain.getMemory(VillagerManager.NEED_ITEM.get()).get();

                                for (ItemStack itemStack : ((AbstractProfession)targetVillager.getVillagerData().getProfession()).getProducibleItems()) {
                                    if (itemStack.sameItem(requestedItem)) {
                                        if (VillagerUtil.hasItemStack(targetVillager, requestedItem) || Produce.canProduce(level, targetVillager, requestedItem)) {
                                            return true;
                                        }
                                    }
                               }
                            }
                        }
                        return false;
                    })
            );
            return targetVillager.isPresent();
        }
        return false;
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long currentTime) {
        if (targetVillager.isPresent() && targetVillager.get() instanceof Villager tradingVillager) {
            Brain<?> target_brain = tradingVillager.getBrain();

            // First villager walks to trading villager
            BehaviorUtils.setWalkAndLookTargetMemories(villager, tradingVillager, 0.5F, 2);

            // Start trading, as long as trading villager isn't trading with someone else.

            //TODO move to precheck, incase villager is trading with someone else.
            Optional<LivingEntity> entity = target_brain.getMemory(VillagerManager.TRADING_ENTITY.get());

            if (villager.distanceTo(tradingVillager) < 3F) {
                // Tell villager he's trading with me
                target_brain.setMemory(VillagerManager.TRADING_ENTITY.get(), villager);

                //TODO check if need item is already set, because this could be the second time
                //TODO If the current villager has a requested item the other villager can produce, then ask for that.
                // otherwise, ask for an amount of emeralds based on the economy's wealth.
                if (target_brain.getMemory(VillagerManager.NEED_ITEM.get()).isEmpty()) {
                    target_brain.setMemory(VillagerManager.NEED_ITEM.get(), new ItemStack(Items.EMERALD));
                }

                BehaviorUtils.lookAtEntity(targetVillager.get(), villager);

                trade(villager, tradingVillager);
            }
        }
    }

    private void trade(Villager villager, Villager tradingVillager) {
        Brain<?> target_brain = tradingVillager.getBrain();
        Brain<?> brain = villager.getBrain();

        if (brain.getMemory(VillagerManager.NEED_ITEM.get()).isPresent() && target_brain.getMemory(VillagerManager.NEED_ITEM.get()).isPresent()) {
            // Make sure they both have their requested items.
            ItemStack farmer = brain.getMemory(VillagerManager.NEED_ITEM.get()).get();
            ItemStack toolsmith = target_brain.getMemory(VillagerManager.NEED_ITEM.get()).get();

            if (VillagerUtil.hasItemStack(villager, toolsmith) && VillagerUtil.hasItemStack(tradingVillager, farmer)) {
                SimpleContainer inv = villager.getInventory();
                SimpleContainer targInv = tradingVillager.getInventory();

                //TODO Make more advanced
                inv.removeItemType(toolsmith.getItem(), 1);
                targInv.removeItemType(farmer.getItem(), 1);

                inv.addItem(farmer);
                targInv.addItem(toolsmith);

                tradeDone = true;
            }
        }

        // If we made it to this point, then clear, everything.
        brain.eraseMemory(VillagerManager.NEED_ITEM.get());
        brain.eraseMemory(VillagerManager.TRADING_ENTITY.get());

        target_brain.eraseMemory(VillagerManager.NEED_ITEM.get());
        target_brain.eraseMemory(VillagerManager.TRADING_ENTITY.get());
    }


    //TODO if they have hoe
    @Override
    protected boolean canStillUse(@NotNull ServerLevel p_22545_, @NotNull Villager p_22546_, long p_22547_) {
        return !tradeDone;
    }
}
