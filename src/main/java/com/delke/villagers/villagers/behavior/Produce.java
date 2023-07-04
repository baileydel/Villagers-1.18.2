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
    private CraftingRecipe recipe;
    private Map<Item, Integer> itemMap = new HashMap<>();
    private final List<TagKey<Item>> acceptedTags = new ArrayList<>();

    public Produce() {
        super(ImmutableMap.of());
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

                        // Convert Recipe into item map
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
                            }
                        }

                        if (satisfies == itemMap.size() && hasSpace(villager.getInventory())) {
                            return true;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }


    protected void start(@NotNull ServerLevel level, @NotNull Villager villager, long time) {
        SimpleContainer inventory = villager.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);

            //TODO TAG CHECK
            // Also if a player has required items in different slots
            if (itemMap.containsKey(itemStack.getItem())) {
                int x = itemMap.get(itemStack.getItem());

                if (itemStack.getCount() >= x ) {
                    itemStack.setCount(itemStack.getCount() - x);
                }
            }
        }
        inventory.addItem(recipe.getResultItem());
        inventory.setChanged();
    }

    protected void tick(@NotNull ServerLevel level, @NotNull Villager villager, long time) {}

    protected void stop(@NotNull ServerLevel level, @NotNull Villager villager, long time) {}


    //TODO when you have like a exact amount and when you craft an item, there will be a space because all of the resources are used.

    // This is going to check if there is at least 1 space available.
    private boolean hasSpace(SimpleContainer villagerInventory) {
        int max = villagerInventory.getContainerSize();
        int satisfies = 0;

        // An empty space is item air
        for (int i = 0; i < max; i++) {
            ItemStack stack = villagerInventory.getItem(i);

            if (recipe != null) {
                if (itemMap.containsKey(stack.getItem())) {
                    int x = itemMap.get(stack.getItem());

                    if (stack.getCount() - x == 0) {
                        satisfies++;
                    }
                }
            }

            if (stack.isEmpty()) {
                satisfies++;
            }
            else if (stack.is(recipe.getResultItem().getItem()) && stack.getCount() + recipe.getResultItem().getCount() < 64) {
                satisfies++;
            }
        }
        return satisfies >= 1;
    }

    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull Villager villager, long p_23206_) {
        return !hasSpace(villager.getInventory());
    }
}
