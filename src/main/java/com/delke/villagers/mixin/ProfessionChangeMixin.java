package com.delke.villagers.mixin;

import com.delke.villagers.villagers.profession.NewVillagerProfession;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.GoalSelector;
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
public abstract class ProfessionChangeMixin {

    @Shadow public abstract VillagerData getVillagerData();

    @Inject(
            method = "registerBrainGoals",
            at = @At("HEAD"),
            cancellable = true
    )
    private void registerBrainGoals(Brain<Villager> brain, CallbackInfo ci) {
        VillagerData data = getVillagerData();
        VillagerProfession profession = data.getProfession();

        if (profession instanceof NewVillagerProfession newProf) {
            newProf.registerBrain(brain);
            newProf.registerGoals((Villager)(Object)this);

            ci.cancel();
            return;
        }

        GoalSelector goalSelector = ((Villager)(Object)this).goalSelector;
        GoalSelector targetSelector = ((Villager)(Object)this).targetSelector;

        if  (targetSelector != null) {
            targetSelector.removeAllGoals();
        }

        if  (goalSelector != null) {
            goalSelector.removeAllGoals();
        }


    }
}
