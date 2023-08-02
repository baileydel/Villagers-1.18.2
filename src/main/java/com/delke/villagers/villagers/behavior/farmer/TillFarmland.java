package com.delke.villagers.villagers.behavior.farmer;

import com.delke.villagers.client.ClientEvents;
import com.delke.villagers.client.debug.VillagerDebugger;
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
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;

public class TillFarmland extends Behavior<Villager> {
   private BlockPos waterSource;
   private List<BlockPos> tillableDirt = new ArrayList<>();

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

      List<BlockPos> waterBlocks = VillagerUtil.searchBlocksWithin(villager, List.of(
              Blocks.WATER
      ), villager.getOnPos(), 1, 3);

      if (waterBlocks.size() == 0) {
         return false;
      }

      waterSource = waterBlocks.get(0);

      tillableDirt = VillagerUtil.searchBlocksWithin(villager, List.of(
              Blocks.DIRT
      ), waterSource, 0, 3);

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
               tillableDirt.remove(0);

               BehaviorUtils.setWalkAndLookTargetMemories(villager, dirt, 0.5F, 1);
            }
         }
      }
   }

   protected void stop(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
      villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

      boolean isClient = !villager.getServer().isDedicatedServer();

      if (isClient) {
         VillagerDebugger debugger = ClientEvents.debuggers.get(villager);
         debugger.searchDebugger.clear();
      }

      tillableDirt.clear();
   }

   protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long p_23206_) {
     return tillableDirt.size() > 0;
   }
}
