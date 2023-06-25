package com.delke.villagers.capability;

import com.delke.villagers.ExampleMod;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * @author Bailey Delker
 * @created 06/24/2023 - 1:50 PM
 * @project Villagers-1.18.2
 */
public class ReputationEvents {

    @SubscribeEvent
    public void AttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(ReputationProvider.PLAYER_REPUTATION).isPresent()) {
                event.addCapability(new ResourceLocation(ExampleMod.MOD_ID, "reputation"), new ReputationProvider());
            }
        }
    }

    @SubscribeEvent
    public void PlayerKilledVillager(LivingDamageEvent event) {
        if (event.getEntity() instanceof Villager villager && event.getSource().getDirectEntity() instanceof ServerPlayer player) {
            int repToTake;
            int amount = (int)event.getAmount();

            if (amount > 1 && amount <= villager.getHealth()) {
                repToTake = amount;
            }
            else if (amount > villager.getHealth()) {
                repToTake = (int)villager.getHealth();
            } else {
                repToTake = 1;
            }

            player.getCapability(ReputationProvider.PLAYER_REPUTATION).ifPresent(playerReputation -> {
                playerReputation.addReputation(-repToTake);
            });
        }
    }

    @SubscribeEvent
    public void PlayerClone(PlayerEvent.Clone event) {

    }

    @SubscribeEvent
    public void RegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Reputation.class);
    }
}
