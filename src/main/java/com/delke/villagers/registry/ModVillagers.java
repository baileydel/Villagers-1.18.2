package com.delke.villagers.registry;

import com.delke.villagers.ExampleMod;
import com.delke.villagers.villagers.profession.Guard;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Bailey Delker
 * @created 06/09/2023 - 10:33 PM
 * @project Villagers-1.18.2
 */
public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, ExampleMod.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, ExampleMod.MOD_ID);
    public static final RegistryObject<PoiType> GUARD_POI = POI_TYPES.register("guard_poi", () -> new PoiType("guard", PoiType.getBlockStates(Blocks.WAXED_COPPER_BLOCK), 1, 1));
    public static final RegistryObject<VillagerProfession> GUARD = VILLAGER_PROFESSIONS.register("guard", Guard::new);

    public static void registerPOIs() {
        try {
            ObfuscationReflectionHelper.findMethod(PoiType.class, "registerBlockStates", PoiType.class).invoke(null, GUARD_POI.get());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        VILLAGER_PROFESSIONS.register(bus);
    }
}
