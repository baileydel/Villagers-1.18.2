package com.delke.villagers.villagers.profession.override;

import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author Bailey Delker
 * @created 07/13/2023 - 5:39 AM
 * @project Villagers-1.18.2
 */
public class NewToolsmith extends AbstractProfession {
    public NewToolsmith() {
        super(
                "newtoolsmith",
                PoiType.TOOLSMITH,
                ImmutableSet.of(),
                ImmutableSet.of(),
                TOOLSMITH.getWorkSound()
        );
    }

    @Override
    public ImmutableList<ItemStack> getProducibleItems() {
        return ImmutableList.of(
                new ItemStack(Items.WOODEN_HOE)
        );
    }
}
