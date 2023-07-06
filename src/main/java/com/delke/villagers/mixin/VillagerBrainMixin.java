package com.delke.villagers.mixin;

import com.delke.villagers.villagers.override.OverrideBrain;
import com.delke.villagers.villagers.profession.AbstractProfession;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Bailey Delker
 * @created 06/16/2023 - 2:54 PM
 * @project Villagers-1.18.2
 */
@Mixin(Villager.class)
public abstract class VillagerBrainMixin {

    @Shadow public abstract VillagerData getVillagerData();

    @Inject(
            method = "registerBrainGoals",
            at = @At("HEAD"),
            cancellable = true
    )
    private void registerBrainGoals(Brain<Villager> brain, CallbackInfo ci) {
        Villager villager = ((Villager)(Object)this);
        VillagerData data = getVillagerData();
        VillagerProfession profession = data.getProfession();

        if (villager.goalSelector != null) {
            villager.targetSelector.removeAllGoals();
            villager.goalSelector.removeAllGoals();
        }

        if (profession instanceof AbstractProfession newProf) {
            newProf.registerBrain(brain, villager);
            newProf.registerGoals(villager);

            ci.cancel();
            return;
        }

        OverrideBrain.DEFAULT_BRAIN(brain, villager);
    }
}
