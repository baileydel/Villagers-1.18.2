package com.delke.villagers.mixin;

import com.delke.villagers.registry.ModVillagers;
import com.delke.villagers.villagers.profession.NewFarmer;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 5:59 PM
 * @project Villagers-1.18.2
 */

@Mixin(VillagerData.class)
public class ProfessionOverrideMixin {
    @Shadow @Final private VillagerType type;
    @Shadow @Final private int level;

    @Inject(
            method = "setProfession",
            at = @At("TAIL"),
            cancellable = true)
    private void setProfession(VillagerProfession profession, CallbackInfoReturnable<VillagerData> cir) {
        if (profession.getName().equals("farmer")) {
            cir.setReturnValue(new VillagerData(type, ModVillagers.NEWFARMER.get(), level));
        }
    }
}
