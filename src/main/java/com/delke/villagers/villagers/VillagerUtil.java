package com.delke.villagers.villagers;

import com.delke.villagers.client.ClientEvents;
import com.delke.villagers.client.debug.VillagerDebugger;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Bailey Delker
 * @created 07/21/2023 - 3:31 AM
 * @project Villagers-1.18.2
 */
public class VillagerUtil {

    public static ItemStack getMatchingClass(Villager villager, Class<? extends Item> type) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            Class<? extends Item> r = stack.getItem().getClass();

            if (r.equals(type)) {
                return stack;
            }

        }
        return ItemStack.EMPTY;
    }

    public static boolean hasMatchingItemTag(Villager villager, TagKey<Item> tag) {
        return getMatchingItemTag(villager, tag) != ItemStack.EMPTY;
    }

    public static ItemStack getMatchingItemTag(Villager villager, TagKey<Item> tag) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.is(tag)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getItem(Villager villager, Item item) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.is(item)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void useItem(Villager villager, ItemStack itemstack) {
        ItemStack stack = villager.getInventory().getItem(0);

        stack.hurtAndBreak(3, villager, (dw) -> {

        });
    }

    public static List<Item> itemPickup(List<TagKey<Item>> tags) {
        List<Item> out = new ArrayList<>();

        for (TagKey<Item> tag : tags) {
            out.addAll(itemPickup(tag));
        }

        return out;
    }

    public static List<Item> itemPickup(TagKey<Item> tag) {
        List<Item> filter = new ArrayList<>();
        List<Item> list = Registry.ITEM.stream().toList();

        for (Item item : list) {
            ItemStack stack = new ItemStack(item);

            if (stack.is(tag)) {
                filter.add(item);
            }
        }
        return filter;
    }

    public static boolean hasItemStack(AbstractVillager villager, ItemStack compare) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (ItemStack.isSame(stack, compare)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEnoughOf(AbstractVillager villager, Item item, int count) {
        return hasEnoughOf(villager, new ItemStack(item, count));
    }

    public static boolean hasEnoughOf(AbstractVillager villager, ItemStack item) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.is(item.getItem())) {
                return (stack.getCount() - item.getCount()) >= 0;
            }
        }
        return false;
    }

    public static ImmutableSet<Item> itemPickup(List<TagKey<Item>> list) {
        List<Item> items = new ArrayList<>();

        for (TagKey<Item> tag : list) {
            items.addAll(itemPickup(tag));
        }

        return ImmutableSet.copyOf(items);
    }

    public static ImmutableSet<Item> itemPickup(TagKey<Item> tag) {
        List<Item> filter = new ArrayList<>();
        List<Item> list = Registry.ITEM.stream().toList();

        for (Item item : list) {
            ItemStack stack = new ItemStack(item);

            System.out.println(item);
            System.out.println("\t\t" + stack.getTags().toList() + "\n");

            if (stack.is(tag)) {
                filter.add(item);
            }
        }
        return ImmutableSet.copyOf(filter);
    }


    //if there is no blockpos, then use villagers position
    // add debug version of search

    //TODO need to add predicate option.
    public static List<BlockPos> searchBlocksWithinBox(@Nonnull Villager villager, List<Block> blocks, BoundingBox box) {
        boolean isClient = !villager.getServer().isDedicatedServer();

        if (isClient) {
            VillagerDebugger debugger = ClientEvents.debuggers.get(villager);
            debugger.searchDebugger.setArea(box);
        }


        List<BlockPos> list = new ArrayList<>();

        for (int x = box.minX(); x < box.maxX(); x++) {
            for (int y = box.minY(); y < box.maxY(); y++) {
                for (int z = box.minZ(); z < box.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = villager.level. getBlockState(pos);

                    for (Block block : blocks) {
                        if (state.is(block)) {
                            list.add(pos);

                            if (isClient) {
                                VillagerDebugger debugger = ClientEvents.debuggers.get(villager);
                                debugger.searchDebugger.addFound(pos);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    public static List<BlockPos> searchBlocksWithinBox(Villager villager, List<Block> blocks, BlockPos pos, int radius) {
        return searchBlocksWithinBox(villager, blocks, new BoundingBox(pos).inflatedBy(radius));
    }

    public static List<BlockPos> searchBlocksWithin(Villager villager, List<Block> blocks, BlockPos pos, int yRadius, int radius) {
        int minX = pos.getX() - radius;
        int maxX = pos.getX() + radius;

        int minY = pos.getY() - yRadius;
        int maxY = (pos.getY()) + yRadius;

        int minZ = pos.getZ() - radius;
        int maxZ = pos.getZ() + radius;

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        return searchBlocksWithinBox(villager, blocks, box);
    }
}
