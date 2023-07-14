package com.delke.villagers;

import com.delke.villagers.capability.Reputation;
import com.delke.villagers.capability.ReputationEvents;
import com.delke.villagers.capability.ReputationProvider;
import com.delke.villagers.client.ClientEvents;
import com.delke.villagers.client.screen.MainScreen;
import com.delke.villagers.client.screen.VillagerInventoryMenu;
import com.delke.villagers.network.ClientboundVillagerScreenOpenPacket;
import com.delke.villagers.network.Network;
import com.delke.villagers.villagers.VillagerManager;
import com.mojang.blaze3d.platform.ScreenManager;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

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
        event.enqueueWork(VillagerManager::registerPOIs);
        Network.init();
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

    @SubscribeEvent
    public void AddVillagerTrades(VillagerTradesEvent event) {
        //TODO this is temp
        if (event.getType() == VillagerManager.overrideMap.get("newfarmer").get()) {

            event.getTrades().put(1, List.of(
                    new EmeraldForItems(Items.WHEAT, 20, 16, 2),
                    new EmeraldForItems(Items.POTATO, 26, 16, 2),
                    new EmeraldForItems(Items.CARROT, 22, 16, 2),
                    new EmeraldForItems(Items.BEETROOT, 15, 16, 2),
                    new ItemsForEmeralds(Items.BREAD, 1, 6, 16, 1))
            );

            event.getTrades().put(2, List.of(
                    new EmeraldForItems(Blocks.PUMPKIN, 6, 12, 10),
                    new ItemsForEmeralds(Items.PUMPKIN_PIE, 1, 4, 5),
                    new ItemsForEmeralds(Items.APPLE, 1, 4, 16, 5)
            ));

            event.getTrades().put(3, List.of(
                    new ItemsForEmeralds(Items.COOKIE, 3, 18, 10),
                    new EmeraldForItems(Blocks.MELON, 4, 12, 20)
            ));

            event.getTrades().put(4, List.of(
                    new ItemsForEmeralds(Blocks.CAKE, 1, 1, 12, 15),
                    new SuspiciousStewForEmerald(MobEffects.NIGHT_VISION, 100, 15),
                    new SuspiciousStewForEmerald(MobEffects.JUMP, 160, 15),
                    new SuspiciousStewForEmerald(MobEffects.WEAKNESS, 140, 15),
                    new SuspiciousStewForEmerald(MobEffects.BLINDNESS, 120, 15),
                    new SuspiciousStewForEmerald(MobEffects.POISON, 280, 15),
                    new SuspiciousStewForEmerald(MobEffects.SATURATION, 7, 15)
            ));

            event.getTrades().put(5, List.of(
                    new ItemsForEmeralds(Items.GOLDEN_CARROT, 3, 3, 30),
                    new ItemsForEmeralds(Items.GLISTERING_MELON_SLICE, 4, 3, 30)
            ));
        }
    }

    static class EmeraldForItems implements VillagerTrades.ItemListing {
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
    static class ItemsForEmeralds implements VillagerTrades.ItemListing {
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