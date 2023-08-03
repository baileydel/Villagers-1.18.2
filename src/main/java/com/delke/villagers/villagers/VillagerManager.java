package com.delke.villagers.villagers;

import com.delke.villagers.ExampleMod;
import com.delke.villagers.villagers.profession.Builder;
import com.delke.villagers.villagers.profession.Guard;
import com.delke.villagers.villagers.profession.LumberJack;
import com.delke.villagers.villagers.profession.override.NewFarmer;
import com.delke.villagers.villagers.profession.override.NewShepherd;
import com.delke.villagers.villagers.profession.override.NewToolsmith;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 07/04/2023 - 3:06 PM
 * @project Villagers-1.18.2
 */
public class VillagerManager {

    public static final Map<String, RegistryObject<VillagerProfession>> overrideMap = new HashMap<>();

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, ExampleMod.MOD_ID);
    public static final RegistryObject<VillagerProfession> GUARD = VILLAGER_PROFESSIONS.register("guard", Guard::new);
    public static final RegistryObject<VillagerProfession> BUILDER = VILLAGER_PROFESSIONS.register("builder", Builder::new);

    public static final RegistryObject<VillagerProfession> LUMBERJACK = VILLAGER_PROFESSIONS.register("lumberjack", LumberJack::new);

    public static final RegistryObject<VillagerProfession> NEWFARMER = registerOverride("newfarmer", new NewFarmer());
    public static final RegistryObject<VillagerProfession> NEWSHEPHERD = registerOverride("newshepherd", new NewShepherd());
    public static final RegistryObject<VillagerProfession> NEWTOOLSMITH = registerOverride("newtoolsmith", new NewToolsmith());

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, ExampleMod.MOD_ID);
    public static final RegistryObject<PoiType> GUARD_POI = POI_TYPES.register("guard_poi", () -> new PoiType("guard", PoiType.getBlockStates(Blocks.WAXED_COPPER_BLOCK), 1, 1));
    public static final RegistryObject<PoiType> BUILDER_POI = POI_TYPES.register("builder_poi", () -> new PoiType("builder", PoiType.getBlockStates(Blocks.CRAFTING_TABLE), 1, 1));
    public static final RegistryObject<PoiType> LUMBERJACK_POI = POI_TYPES.register("lumberjack_poi", () -> new PoiType("lumberjack", PoiType.getBlockStates(Blocks.BEDROCK), 1, 1));

    public static final DeferredRegister<MemoryModuleType<?>> VILLAGER_MEMORIES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, ExampleMod.MOD_ID);
    public static final RegistryObject<MemoryModuleType<ItemStack>> NEED_ITEM = VILLAGER_MEMORIES.register("neededitem", () -> new MemoryModuleType<>(Optional.empty()));

    static RegistryObject<VillagerProfession> registerOverride(String name, VillagerProfession profession) {
        RegistryObject<VillagerProfession> pr = VILLAGER_PROFESSIONS.register(name, () -> profession);
        overrideMap.put(name, pr);
        return pr;
    }

    public static void registerPOIs() {
        try {
            ObfuscationReflectionHelper.findMethod(PoiType.class, "registerBlockStates", PoiType.class).invoke(null, GUARD_POI.get());
            ObfuscationReflectionHelper.findMethod(PoiType.class, "registerBlockStates", PoiType.class).invoke(null, LUMBERJACK_POI.get());
            ObfuscationReflectionHelper.findMethod(PoiType.class, "registerBlockStates", PoiType.class).invoke(null, BUILDER_POI.get());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        VILLAGER_PROFESSIONS.register(bus);
        VILLAGER_MEMORIES.register(bus);
    }
}
