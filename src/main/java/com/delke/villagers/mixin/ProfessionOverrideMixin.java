package com.delke.villagers.mixin;

import com.delke.villagers.villagers.VillagerManager;
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
            cancellable = true
    )
    private void setProfession(VillagerProfession profession, CallbackInfoReturnable<VillagerData> cir) {
        String profName = profession.getName();

        if (!profName.equals("none")) {
            VillagerProfession override = VillagerManager.overrideMap.get("new" + profName).get();

            if (override != null) {
                cir.setReturnValue(new VillagerData(type, override, level));
            }
        }
    }
}
