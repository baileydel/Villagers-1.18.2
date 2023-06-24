package com.delke.villagers;

import com.delke.villagers.client.ClientEvents;
import com.delke.villagers.client.screen.VillagerInventoryMenu;
import com.delke.villagers.network.ClientboundVillagerScreenOpenPacket;
import com.delke.villagers.network.Network;
import com.delke.villagers.registry.ModVillagers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;

@Mod("villagers")
public class ExampleMod {
    public static final String MOD_ID = "villagers";

    public ExampleMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        ModVillagers.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void ClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void CommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModVillagers::registerPOIs);
        Network.init();
    }

    @SubscribeEvent
    public void EntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide().isServer()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (player.isCrouching() && event.getTarget() instanceof Villager villager) {
                    SimpleContainer container = villager.getInventory();
                    Inventory inventory = player.getInventory();

                    player.nextContainerCounter();
                    VillagerInventoryMenu menu = new VillagerInventoryMenu(player.containerCounter, inventory, container);
                    player.containerMenu = menu;

                    Network.INSTANCE.sendTo(new ClientboundVillagerScreenOpenPacket(player.containerCounter, container.getContainerSize(), villager.getId()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    player.initMenu(menu);
                    MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
                }
            }
        }
    }

}