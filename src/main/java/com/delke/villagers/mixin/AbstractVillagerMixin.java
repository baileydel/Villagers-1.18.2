package com.delke.villagers.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 5:59 PM
 * @project Villagers-1.18.2
 */

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin implements ContainerListener {

    @Shadow
    private SimpleContainer inventory;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(EntityType<? extends AbstractVillager> p_35267_, Level p_35268_, CallbackInfo ci) {
        inventory = new SimpleContainer(9);
        inventory.addListener(this);
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
    public void containerChanged(@NotNull Container container) {}
}
