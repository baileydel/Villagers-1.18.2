package com.delke.villagers.villagers.behavior.farmer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class HarvestCrops extends Behavior<Villager> {
    @Nullable
    private BlockPos currentBlock;

    private final List<BlockPos> validFarmland = Lists.newArrayList();

    public HarvestCrops() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.SECONDARY_JOB_SITE, MemoryStatus.VALUE_PRESENT)
        );
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
        if (ForgeEventFactory.getMobGriefingEvent(level, villager)) {
            this.validFarmland.clear();

            BlockPos.MutableBlockPos blockPos = villager.blockPosition().mutable();

            for (int x = -2; x <= 2; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    for (int z = -2; z <= 2; ++z) {

                        BlockState state = level.getBlockState(blockPos);

                        if (state.is(Blocks.FARMLAND)) {
                            this.validFarmland.add(new BlockPos(blockPos));
                        }

                        blockPos.set(villager.getX() + x, villager.getY() + y, villager.getZ() + z);
                    }
                }
            }

            if (validFarmland.size() > 0) {
                for (BlockPos pos : validFarmland) {
                    BlockState crop = level.getBlockState(pos.above());

                    // If block is empty
                    // and villager has seeds, then plant
                    if (crop.is(Blocks.AIR) && villager.getInventory().hasAnyOf(Set.of(Items.WHEAT_SEEDS))) {
                        currentBlock = pos;
                        return true;
                    }
                    else if (crop.is(BlockTags.CROPS)) {
                        currentBlock = pos.above();
                        return true;
                    }
                }
            }
        }
        return false;
    }


    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long cur_time) {
        if (currentBlock != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(villager, currentBlock, 0.5F, 1);
        }
    }


    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long p_22553_) {
        if (validFarmland.size() > 0) {
            BehaviorUtils.setWalkAndLookTargetMemories(villager, currentBlock, 0.5F, 1);
        }



        /*
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

        if (this.currentBlock == null || this.currentBlock.closerToCenterThan(villager.position(), 1.0D)) {
            if (this.currentBlock != null && p_23198_ > this.nextOkStartTime) {
                BlockState blockstate = level.getBlockState(this.currentBlock);
                Block block = blockstate.getBlock();
                Block block1 = level.getBlockState(this.currentBlock.below()).getBlock();

                if (block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockstate)) {
                    level.destroyBlock(this.currentBlock, true, villager);
                }

                if (blockstate.isAir() && block1 instanceof FarmBlock && villager.hasFarmSeeds()) {
                    SimpleContainer simplecontainer = villager.getInventory();

                    for(int i = 0; i < simplecontainer.getContainerSize(); ++i) {
                        ItemStack itemstack = simplecontainer.getItem(i);
                        boolean flag = false;
                        if (!itemstack.isEmpty()) {
                            if (itemstack.is(Items.WHEAT_SEEDS)) {
                                level.setBlock(this.currentBlock, Blocks.WHEAT.defaultBlockState(), 3);
                                flag = true;
                            } else if (itemstack.is(Items.POTATO)) {
                                level.setBlock(this.currentBlock, Blocks.POTATOES.defaultBlockState(), 3);
                                flag = true;
                            } else if (itemstack.is(Items.CARROT)) {
                                level.setBlock(this.currentBlock, Blocks.CARROTS.defaultBlockState(), 3);
                                flag = true;
                            } else if (itemstack.is(Items.BEETROOT_SEEDS)) {
                                level.setBlock(this.currentBlock, Blocks.BEETROOTS.defaultBlockState(), 3);
                                flag = true;
                            } else if (itemstack.getItem() instanceof net.minecraftforge.common.IPlantable) {
                                if (((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlantType(level, currentBlock) == PlantType.CROP) {
                                    level.setBlock(currentBlock, ((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlant(level, currentBlock), 3);
                                    flag = true;
                                }
                            }
                        }

                        if (flag) {
                            level.playSound(null, this.currentBlock.getX(), this.currentBlock.getY(), this.currentBlock.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                simplecontainer.setItem(i, ItemStack.EMPTY);
                            }
                            break;
                        }
                    }
                }

                if (block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockstate)) {
                    this.validFarmland.remove(this.currentBlock);
                    this.currentBlock = this.getValidFarmland(level);
                    if (this.currentBlock != null) {
                        this.nextOkStartTime = p_23198_ + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.currentBlock), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.currentBlock));
                    }
                }
            }
            ++this.timeWorkedSoFar;
        }
         */
    }

    protected void stop(@NotNull ServerLevel level, Villager villager, long p_23190_) {
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, Villager p_22546_, long p_22547_) {
        return true;
    }
}
