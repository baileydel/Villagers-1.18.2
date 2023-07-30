package com.delke.villagers;

import com.delke.villagers.capability.Reputation;
import com.delke.villagers.capability.ReputationProvider;
import com.delke.villagers.network.ClientboundVillagerScreenOpenPacket;
import com.delke.villagers.network.Network;
import com.delke.villagers.villagers.VillagerUtil;
import com.delke.villagers.villagers.profession.LumberJack;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 10:58 AM
 * @project Villagers-1.18.2
 */
public class CommonEvents {

    @SubscribeEvent
    public void SaplingGrow(SaplingGrowTreeEvent event) {
        ServerLevel serverlevel = (ServerLevel)event.getWorld();

        AABB box = new AABB(event.getPos()).inflate(32);

        List<Villager> list = serverlevel.getEntities(EntityType.VILLAGER, box, this::isLumberjack);

        for (Villager villager : list) {
            if (VillagerUtil.hasItemStack(villager, new ItemStack(Items.WOODEN_AXE))) {
                Optional<List<GlobalPos>> optional = villager.getBrain().getMemory(MemoryModuleType.SECONDARY_JOB_SITE);

                List<GlobalPos> list1 = optional.orElseGet(ArrayList::new);

                list1.add(GlobalPos.of(serverlevel.dimension(), event.getPos()));

                villager.getBrain().setMemory(MemoryModuleType.SECONDARY_JOB_SITE, list1);

                break;
            }
        }
    }

    private boolean isLumberjack(Villager villager) {
        VillagerData data = villager.getVillagerData();
        return data.getProfession() instanceof LumberJack;
    }

    @SubscribeEvent
    public void EntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide().isServer()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (event.getTarget() instanceof Villager villager) {
                    Reputation reputation = player.getCapability(ReputationProvider.PLAYER_REPUTATION)
                            .orElseThrow(() -> new RuntimeException("Failed to access player reputation."));

                    if (reputation.get() < 0) {
                        villager.setUnhappyCounter(40);
                        villager.playSound(SoundEvents.VILLAGER_NO, 1, villager.getVoicePitch());

                        if (event.isCancelable()) {
                            event.setCanceled(true);
                        }
                    }

                    if (player.isCrouching()) {
                        SimpleContainer container = villager.getInventory();
                        Inventory inventory = player.getInventory();

                        player.nextContainerCounter();
                        VillagerInventoryMenu menu = new VillagerInventoryMenu(player.containerCounter, inventory, container);
                        player.containerMenu = menu;

                        Network.INSTANCE.sendTo(new ClientboundVillagerScreenOpenPacket(player.containerCounter, container.getContainerSize(), villager.getId()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                        player.initMenu(menu);
                        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
                    }
                    //TODO REMOVE
                    else {
                        player.getCapability(ReputationProvider.PLAYER_REPUTATION).ifPresent(playerReputation -> {
                            playerReputation.addReputation(1);
                            player.sendMessage(new TextComponent("added 1 - " + playerReputation.get()) , UUID.randomUUID());
                        });
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void RegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Reputation.class);
    }

    public static class EmeraldForItems implements VillagerTrades.ItemListing {
        private final Item item;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public EmeraldForItems(ItemLike p_35657_, int p_35658_, int p_35659_, int p_35660_) {
            this.item = p_35657_.asItem();
            this.cost = p_35658_;
            this.maxUses = p_35659_;
            this.villagerXp = p_35660_;
            this.priceMultiplier = 0.05F;
        }

        public MerchantOffer getOffer(@NotNull Entity p_35662_, @NotNull Random p_35663_) {
            ItemStack itemstack = new ItemStack(this.item, this.cost);
            return new MerchantOffer(itemstack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }
    public static class ItemsForEmeralds implements VillagerTrades.ItemListing {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int numberOfItems;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public ItemsForEmeralds(Block p_35765_, int p_35766_, int p_35767_, int p_35768_, int p_35769_) {
            this(new ItemStack(p_35765_), p_35766_, p_35767_, p_35768_, p_35769_);
        }

        public ItemsForEmeralds(Item p_35741_, int p_35742_, int p_35743_, int p_35744_) {
            this(new ItemStack(p_35741_), p_35742_, p_35743_, 12, p_35744_);
        }

        public ItemsForEmeralds(Item p_35746_, int p_35747_, int p_35748_, int p_35749_, int p_35750_) {
            this(new ItemStack(p_35746_), p_35747_, p_35748_, p_35749_, p_35750_);
        }

        public ItemsForEmeralds(ItemStack p_35752_, int p_35753_, int p_35754_, int p_35755_, int p_35756_) {
            this(p_35752_, p_35753_, p_35754_, p_35755_, p_35756_, 0.05F);
        }

        public ItemsForEmeralds(ItemStack p_35758_, int p_35759_, int p_35760_, int p_35761_, int p_35762_, float p_35763_) {
            this.itemStack = p_35758_;
            this.emeraldCost = p_35759_;
            this.numberOfItems = p_35760_;
            this.maxUses = p_35761_;
            this.villagerXp = p_35762_;
            this.priceMultiplier = p_35763_;
        }

        public MerchantOffer getOffer(Entity p_35771_, Random p_35772_) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(this.itemStack.getItem(), this.numberOfItems), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }
    static class SuspiciousStewForEmerald implements VillagerTrades.ItemListing {
        final MobEffect effect;
        final int duration;
        final int xp;
        private final float priceMultiplier;

        public SuspiciousStewForEmerald(MobEffect p_186313_, int p_186314_, int p_186315_) {
            this.effect = p_186313_;
            this.duration = p_186314_;
            this.xp = p_186315_;
            this.priceMultiplier = 0.05F;
        }

        @Nullable
        public MerchantOffer getOffer(@NotNull Entity p_186317_, @NotNull Random p_186318_) {
            ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.saveMobEffect(itemstack, this.effect, this.duration);
            return new MerchantOffer(new ItemStack(Items.EMERALD, 1), itemstack, 12, this.xp, this.priceMultiplier);
        }
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeModificationEvent event) {
            if (!event.has(EntityType.VILLAGER, Attributes.ATTACK_DAMAGE)) {
                event.add(EntityType.VILLAGER, Attributes.ATTACK_DAMAGE);
            }
        }
    }
}
