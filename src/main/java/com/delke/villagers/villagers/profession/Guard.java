package com.delke.villagers.villagers.profession;

import com.delke.villagers.capability.ReputationProvider;
import com.delke.villagers.villagers.behavior.Produce;
import com.delke.villagers.villagers.behavior.ReactToReputation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.delke.villagers.villagers.VillagerManager.GUARD_POI;


/**
 * @author Bailey Delker
 * @created 06/13/2023 - 8:51 PM
 * @project Villagers-1.18.2
 */
public class Guard extends AbstractProfession {
    public Guard() {
        super("guard",
                GUARD_POI.get(),
                ImmutableSet.of(),
                SoundEvents.EVOKER_CAST_SPELL
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWorkCorePackage() {
        List<Pair<Integer, ? extends Behavior<? super Villager>>> t = new ArrayList<Pair<Integer, ? extends Behavior<? super Villager>>>(List.of(
                //Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, 0.5F, 9, 100, 1200)),

                Pair.of(5, new RunOne<>(
                        getRunOnePackage()
                )),

                getMinimalLookBehavior(),

                Pair.of(99, new UpdateActivityFromSchedule())
        ));

        if (isProducer()) {
            t.add(Pair.of(10, new ShowTradesToPlayer(400, 1600)));
            t.add(Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)));
        }

        return ImmutableList.copyOf(t);
    }

    public List<Pair<Behavior<? super Villager>, Integer>> getRunOnePackage() {
        //TODO make this more advanced

        // Every Villager will go to their work site.
        List<Pair<Behavior<? super Villager>, Integer>> t = new ArrayList<>(List.of(
                //Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 2, 10), 5)
        ));

        t.addAll(getWorkPOIBehaviorPackage());

        // Every Producer will produce
        if (isProducer()) {
            t.add(Pair.of(new Produce(), 1));
        }

        // Any additional behaviors
        t.addAll(getSecondWorkPackage());

        return t;
    }

    @Override
    public Pair<Behavior<? super Villager>, Integer> getWorkPOIBehavior() {
        return Pair.of(new WorkAtPoi(), 7);
    }

    @Override
    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new ReactToReputation(EntityType.PLAYER, 16F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new WakeUp()),

                Pair.of(0, new SetRaidStatus()),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new PoiCompetitorScan(this)),
                Pair.of(5, new GoToWantedItem<>(0.5f, false, 4)),
                Pair.of(6, new AcquirePoi(getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new GoToPotentialJobSite(0.5f)),
                Pair.of(8, new YieldJobSite(0.5f)),
                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte) 14))),
                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte) 14))),
                Pair.of(10, new AssignProfessionFromJobSite()),
                Pair.of(10, new ResetProfession())
        );
    }

    @Override
    public void registerGoals(Villager villager) {
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.0D, true));
        villager.goalSelector.addGoal(2, new MoveTowardsTargetGoal(villager, 0.9D, 32.0F));
        villager.goalSelector.addGoal(2, new MoveBackToVillageGoal(villager, 0.6D, false));
        villager.goalSelector.addGoal(7, new LookAtPlayerGoal(villager, Player.class, 6.0F));
        villager.goalSelector.addGoal(8, new RandomLookAroundGoal(villager));
        villager.targetSelector.addGoal(2, new HurtByTargetGoal(villager));
        villager.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(villager, LivingEntity.class, 5, false, false,
            (entity) -> {
                if (entity instanceof ServerPlayer player) {
                    AtomicBoolean attack = new AtomicBoolean(false);
                    player.getCapability(ReputationProvider.PLAYER_REPUTATION).ifPresent(reputation -> {
                        if (reputation.get() < 0) {
                            attack.set(true);
                        }
                    });
                    return true;
                }
                return entity instanceof Enemy && !(entity instanceof Creeper);
            }
        ));
    }
}
