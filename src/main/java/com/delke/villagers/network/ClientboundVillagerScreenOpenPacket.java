package com.delke.villagers.network;

import com.delke.villagers.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundVillagerScreenOpenPacket {
   private final int containerId;
   private final int size;
   private final int entityId;

   public ClientboundVillagerScreenOpenPacket(int containerId, int size, int entityId) {
      this.containerId = containerId;
      this.size = size;
      this.entityId = entityId;
   }

   public ClientboundVillagerScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
      this.containerId = friendlyByteBuf.readUnsignedByte();
      this.size = friendlyByteBuf.readVarInt();
      this.entityId = friendlyByteBuf.readInt();
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeByte(this.containerId);
      buf.writeVarInt(this.size);
      buf.writeInt(this.entityId);
   }

   public static void handle(ClientboundVillagerScreenOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() ->
              DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handVillagerScreenPacket(msg, ctx)));
      ctx.get().setPacketHandled(true);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getEntityId() {
      return this.entityId;
   }
}