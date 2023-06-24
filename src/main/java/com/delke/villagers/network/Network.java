package com.delke.villagers.network;

import com.delke.villagers.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project Villagers-1.18.2
 */
public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(0, ClientboundVillagerScreenOpenPacket.class, ClientboundVillagerScreenOpenPacket::write, ClientboundVillagerScreenOpenPacket::new, ClientboundVillagerScreenOpenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
   }
}
