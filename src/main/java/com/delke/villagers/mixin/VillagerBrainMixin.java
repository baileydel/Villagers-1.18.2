package com.delke.villagers.mixin;

import com.delke.villagers.villagers.OverrideBrain;
import com.delke.villagers.villagers.VillagerManager;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Bailey Delker
 * @created 06/16/2023 - 2:54 PM
 * @project Villagers-1.18.2
 */
@Mixin(Villager.class)
public abstract class VillagerBrainMixin {

    @Shadow public abstract VillagerData getVillagerData();

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.JOB_SITE,
            MemoryModuleType.POTENTIAL_JOB_SITE,
            MemoryModuleType.MEETING_POINT,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.VISIBLE_VILLAGER_BABIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_BED,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_HOSTILE,
            MemoryModuleType.SECONDARY_JOB_SITE,
            MemoryModuleType.HIDING_PLACE,
            MemoryModuleType.HEARD_BELL_TIME,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            MemoryModuleType.LAST_WORKED_AT_POI,
            MemoryModuleType.GOLEM_DETECTED_RECENTLY,
            VillagerManager.NEED_ITEM.get(),
            VillagerManager.TRADING_ENTITY.get()
    );


    @Shadow @Final private static ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES;

    @Inject(
            method = "brainProvider",
            at = @At("TAIL"),
            cancellable = true
    )
    protected void brainProvider(CallbackInfoReturnable<Brain.Provider<Villager>> cir) {
        cir.setReturnValue(Brain.provider(MEMORY_TYPES, SENSOR_TYPES));
    }


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
