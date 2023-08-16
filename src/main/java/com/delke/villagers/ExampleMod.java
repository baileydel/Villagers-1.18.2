package com.delke.villagers;

import com.delke.villagers.capability.ReputationEvents;
import com.delke.villagers.client.ClientEvents;
import com.delke.villagers.network.Network;
import com.delke.villagers.villagers.VillagerManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("villagers")
public class ExampleMod {
    public static final String MOD_ID = "villagers";

    public ExampleMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        VillagerManager.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ReputationEvents());
    }

    private void ClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void CommonSetup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        event.enqueueWork(VillagerManager::registerPOIs);
        Network.init();
    }
}