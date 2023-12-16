package com.delke.villagers.villages;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class Building {

    BlockPos position = new BlockPos(0,-60, 2);
    BlockPos postion2 = new BlockPos(-7, -56, -7);


    String name = "lumberHouse";

    public BlockPos getPosition() {
        return position;
    }
    public BlockPos getPosition2() {
        return postion2;
    }

    private boolean isBuilt(){
        return true;
    }
}
