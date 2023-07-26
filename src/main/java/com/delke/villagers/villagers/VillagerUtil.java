package com.delke.villagers.villagers;

import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

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

    public static boolean hasItemStack(Villager villager, ItemStack compare) {
        SimpleContainer inv = villager.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (ItemStack.isSame(stack, compare)) {
                return true;
            }
        }
        return false;
    }


}
