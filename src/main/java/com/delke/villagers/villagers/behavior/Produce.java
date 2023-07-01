package com.delke.villagers.villagers.behavior;

import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 06/29/2023 - 11:48 PM
 * @project Villagers-1.18.2
 */
public class Produce extends Behavior<Villager> {
    private Map<Item, Integer> itemMap;
    private CraftingRecipe recipe;
    private List<TagKey<Item>> acceptedTags = new ArrayList<>();

    public Produce() {
        super(ImmutableMap.of());
    }

    //TODO STOPSHIP check if the item has an accepted tag.
    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        SimpleContainer inventory = villager.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemMap.containsKey(itemStack.getItem())) {
                int x = itemMap.get(itemStack.getItem());

                if (itemStack.getCount() >= x ) {
                    itemStack.setCount(itemStack.getCount() - x);
                }
                inventory.addItem(recipe.getResultItem());
            }
        }
    }

    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long time) {

    }

    protected void stop(@NotNull ServerLevel level, @NotNull Villager villager, long time) {

    }

    //TODO if the villager even has space? if not, it will need to make some
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull Villager villager) {
        AbstractProfession profession =  (AbstractProfession) villager.getVillagerData().getProfession();
        ImmutableList<Item> items = profession.getProducibleItems();

        if (profession.isProducer() && items.size() > 0) {
            List<CraftingRecipe> optional = level.getServer().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

            //TODO using map methods, instead of using for loops is the best option here, but this is a plan of attack ig
            for (Item producible : items) {
                for (CraftingRecipe recipe : optional) {
                    if (recipe.getId().equals(producible.getRegistryName())) {
                        this.recipe = recipe;
                        this.itemMap = new HashMap<>();

                        List<Ingredient> ingredients = recipe.getIngredients();

                        for (Ingredient ingredient : ingredients) {
                            ItemStack[] ing = ingredient.getItems();

                            for (int i = 0; i < ing.length; i++) {
                                ItemStack stack = ing[i];
                                List<TagKey<Item>> l = stack.getTags().toList();

                                if (l.size() > 0 && ing.length > 1 && i > 0) {
                                    if (!acceptedTags.contains(l.get(0))) {
                                        acceptedTags.add(l.get(0));
                                    }
                                    break;
                                }

                                int c = stack.getCount();
                                if (itemMap.containsKey(stack.getItem())) {
                                    c += itemMap.get(stack.getItem());
                                }
                                itemMap.put(stack.getItem().asItem(), c);
                            }
                        }

                        int satisfies = 0;
                        for (int i = 0; i < villager.getInventory().getContainerSize(); i++) {
                            ItemStack stack = villager.getInventory().getItem(i);

                            if (itemMap.containsKey(stack.getItem())) {
                                if (stack.getCount() >= itemMap.get(stack.getItem())) {
                                    satisfies++;
                                }

                                if (satisfies == itemMap.size()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long p_23206_) {
        return true;
    }
}
