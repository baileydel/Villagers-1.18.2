package com.delke.villagers.villagers.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;

/**
 * @author Bailey Delker
 * @created 06/24/2023 - 11:14 AM
 * @project Villagers-1.18.2
 */
public class ReactToReputation extends Behavior<Villager> {
    public ReactToReputation() {
        super(ImmutableMap.of());
    }
}
