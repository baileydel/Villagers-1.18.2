package com.delke.villagers.villagers.behavior.farmer;

import com.delke.villagers.villagers.VillagerManager;
import com.delke.villagers.villagers.VillagerUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;

public class TillFarmland extends Behavior<Villager> {
   private BlockPos waterSource;
   private final List<BlockPos> tillableDirt = new ArrayList<>();
   private int timeWorked = 0;

   public TillFarmland() {
      super(
              ImmutableMap.of(
                      MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                      MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
              )
      );
   }

   protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
      // Search for hoe
      boolean f = VillagerUtil.getMatchingClass(villager, HoeItem.class) != ItemStack.EMPTY;

      if (!f) {
         villager.getBrain().setMemory(VillagerManager.NEED_ITEM.get(), new ItemStack(Items.WOODEN_HOE));
         System.out.println("cannot find hoe, going to request");
         return false;
      }

      if (tillableDirt.size() == 0) {
         BlockPos.MutableBlockPos blockPos = villager.blockPosition().mutable();
         boolean found = false;

         for (int x = -2; x <= 2 && !found; ++x) {
            for (int y = -1; y <= 1 && !found; ++y) {
               for (int z = -2; z <= 2; ++z) {
                  BlockState state = level.getBlockState(blockPos);

                  if (state.is(Blocks.WATER)) {
                     waterSource = blockPos;
                     found = true;
                     break;
                  }

                  blockPos.set(villager.getX() + x, villager.getY() + y, villager.getZ() + z);
               }
            }
         }

         if (waterSource != null) {
            blockPos = waterSource.mutable();

            for (int x = -4; x <= 4; ++x) {
               for (int z = -4; z <= 4; ++z) {
                  blockPos.set(waterSource.getX() + x , waterSource.getY(), waterSource.getZ() + z);
                  BlockState state = level.getBlockState(blockPos);

                  if (!tillableDirt.contains(blockPos)) {
                     if (state.is(Blocks.DIRT) && !state.is(Blocks.FARMLAND)) {
                        BlockState above = level.getBlockState(blockPos.above());

                        if (above.is(Blocks.AIR)) {
                           tillableDirt.add(new BlockPos(blockPos));
                        }
                     }
                  }
               }
            }
         }
      }
      return tillableDirt.size() > 0;
   }

   protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
      if (this.waterSource != null && tillableDirt.size() > 0) {
         BehaviorUtils.setWalkAndLookTargetMemories(villager, tillableDirt.get(0), 0.5F, 1);
      }
   }

   protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
      if (tillableDirt.size() > 0) {
         BlockPos dirt = tillableDirt.get(0);

         if (dirt.closerToCenterThan(villager.position(), 1.5D)) {
            level.playSound(null, dirt, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(dirt, Blocks.FARMLAND.defaultBlockState(), 3);

            tillableDirt.remove(0);

            if (tillableDirt.size() > 0) {
               dirt = tillableDirt.get(0);

               BehaviorUtils.setWalkAndLookTargetMemories(villager, dirt, 0.5F, 1);
            }
         }
      }
      timeWorked++;
   }

   protected void stop(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
      villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      tillableDirt.clear();
      timeWorked = 0;
   }

   protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long p_23206_) {
     return timeWorked < 200;
   }
}
