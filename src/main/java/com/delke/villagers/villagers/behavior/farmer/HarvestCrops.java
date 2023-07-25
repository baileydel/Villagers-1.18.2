package com.delke.villagers.villagers.behavior.farmer;

import com.delke.villagers.villagers.VillagerUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HarvestCrops extends Behavior<Villager> {
    private final List<BlockPos> validFarmland = Lists.newArrayList();
    private BlockPos currentBlock;

    public HarvestCrops() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.SECONDARY_JOB_SITE, MemoryStatus.VALUE_PRESENT)
        );
    }

    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
        if (ForgeEventFactory.getMobGriefingEvent(level, villager)) {
            validFarmland.clear();

            BlockPos.MutableBlockPos pos = villager.blockPosition().mutable();

            for (int x = -3; x <= 3; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockState state = level.getBlockState(pos);

                        if (state.is(Blocks.FARMLAND)) {
                            BlockState above = level.getBlockState(pos.above());

                            boolean add = false;
                            if (above.is(BlockTags.CROPS)) {
                                CropBlock crop_block = (CropBlock) above.getBlock();
                                add = crop_block.isMaxAge(above);
                            }

                            if (add || (above.isAir() && VillagerUtil.hasMatchingItemTag(villager, Tags.Items.SEEDS))) {
                                this.validFarmland.add(new BlockPos(pos));
                            }
                        }
                        pos.set(villager.getX() + x, villager.getY() + y, villager.getZ() + z);
                    }
                }
            }
        }
        return validFarmland.size() > 0;
    }

    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        if (validFarmland.size() > 0) {
            for (BlockPos pos : validFarmland) {
                BlockState crop = level.getBlockState(pos.above());

                if (crop.is(Blocks.AIR) && VillagerUtil.hasMatchingItemTag(villager, Tags.Items.SEEDS)) {
                    currentBlock = pos;
                }
                else if (crop.is(BlockTags.CROPS)) {
                    CropBlock crop_block = (CropBlock)crop.getBlock();

                    if (crop_block.isMaxAge(crop)) {
                        currentBlock = pos;
                    }
                }
            }
        }

        if (currentBlock != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(villager, currentBlock, 0.5F, 1);
        }
    }

    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        if (currentBlock != null) {
            if (validFarmland.size() > 0 && currentBlock.closerToCenterThan(villager.position(), 1.5D)) {
                currentBlock = currentBlock.above();

                BlockState state = level.getBlockState(currentBlock);

                if (state.is(BlockTags.CROPS)) {
                    CropBlock crop = (CropBlock)state.getBlock();

                    if (crop.isMaxAge(state)) {
                        level.destroyBlock(currentBlock, true);
                    }
                }


                SimpleContainer simplecontainer = villager.getInventory();
                for (int i = 0; i < simplecontainer.getContainerSize(); ++i) {
                    ItemStack itemstack = simplecontainer.getItem(i);

                    if (!itemstack.isEmpty()) {
                        Item item = itemstack.getItem();

                        if (itemstack.is(Tags.Items.SEEDS) || item instanceof IPlantable) {
                            if (item instanceof BlockItem blockItem) {
                                Block block =  blockItem.getBlock();
                                level.setBlock(currentBlock, block.defaultBlockState(), 3);
                                level.playSound(null, currentBlock.getX(), currentBlock.getY(), currentBlock.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);

                                itemstack.shrink(1);
                                if (itemstack.isEmpty()) {
                                    simplecontainer.setItem(i, ItemStack.EMPTY);
                                }

                                if (validFarmland.size() > 0) {
                                    validFarmland.remove(currentBlock.below());
                                    if (validFarmland.size() > 0) {
                                        currentBlock = validFarmland.get(0);
                                        BehaviorUtils.setWalkAndLookTargetMemories(villager, currentBlock, 0.5F, 1);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void stop(@NotNull ServerLevel level, Villager villager, long time) {
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        return validFarmland.size() > 0;
    }
}
