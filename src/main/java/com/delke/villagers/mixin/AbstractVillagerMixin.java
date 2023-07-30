package com.delke.villagers.mixin;

import com.delke.villagers.ExampleMod;
import com.delke.villagers.villagers.VillagerUtil;
import com.delke.villagers.villagers.profession.AbstractProfession;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 5:59 PM
 * @project Villagers-1.18.2
 */

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin implements ContainerListener {

    @Shadow
    private SimpleContainer inventory;

    @Shadow @Nullable protected MerchantOffers offers;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(EntityType<? extends AbstractVillager> p_35267_, Level p_35268_, CallbackInfo ci) {
        inventory = new SimpleContainer(9);
        inventory.addListener(this);
    }

    @Inject(
            method = "notifyTrade",
            at = @At("TAIL")
    )
    private void notifyTrade(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager villager = (AbstractVillager)(Object)this;
        SimpleContainer inv = villager.getInventory();
        ItemStack offerStack = offer.getResult();

        if (VillagerUtil.hasEnoughOf(villager, offerStack)) {
            inv.removeItemType(offerStack.getItem(), offerStack.getCount());
        }
    }

    @Inject(
            method = "canBeLeashed",
            at = @At("TAIL"),
            cancellable = true
    )
    private void canBeLeashed(Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Override
    public void containerChanged(@NotNull Container container) {
        Villager villager = ((Villager)(Object)this);
        VillagerData data = villager.getVillagerData();
        VillagerProfession profession = data.getProfession();

        MerchantOffers newOffers = new MerchantOffers();

        // Add new trades when villagers inventory changes.
        if (profession instanceof AbstractProfession abstractProfession) {
            SimpleContainer inventory = villager.getInventory();
            offers = newOffers;

            for (ItemStack stack : abstractProfession.getProducibleItems()) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack item = inventory.getItem(i);

                    int count = item.getCount() / 6;

                    if (stack.is(item.getItem()) && count >= 1) {
                        offers.add(new ExampleMod.ItemsForEmeralds(item.getItem(), 1, 6, count, 1).getOffer(villager, villager.getRandom()));
                    }
                }
            }
        }
    }
}
