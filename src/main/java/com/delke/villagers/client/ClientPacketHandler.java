package com.delke.villagers.client;

import com.delke.villagers.network.ClientboundVillagerScreenOpenPacket;
import com.delke.villagers.VillagerInventoryMenu;
import com.delke.villagers.client.screen.VillagerInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 06/21/2023 - 11:10 PM
 * @project Villagers-1.18.2
 */
public class ClientPacketHandler {

    public static void handVillagerScreenPacket(ClientboundVillagerScreenOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (mc.level != null && player != null) {
            Entity entity = mc.level.getEntity(msg.getEntityId());

            if (entity instanceof AbstractVillager villager) {
                VillagerInventoryMenu villagerInventoryMenu = new VillagerInventoryMenu(msg.getContainerId(), player.getInventory(), villager.getInventory());

                player.containerMenu = villagerInventoryMenu;
                mc.setScreen(new VillagerInventoryScreen(villagerInventoryMenu, player.getInventory()));
            }
        }
    }
}
