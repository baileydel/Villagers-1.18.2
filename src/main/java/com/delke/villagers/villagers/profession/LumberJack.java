package com.delke.villagers.villagers.profession;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;

import static com.delke.villagers.villagers.VillagerManager.LUMBERJACK_POI;

/**
 * @author Bailey Delker
 * @created 07/22/2023 - 1:16 PM
 * @project Villagers-1.18.2
 */
public class LumberJack extends AbstractProfession {
    public LumberJack() {
        super(
                "lumberjack",
                LUMBERJACK_POI.get(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD
        );
    }


}
