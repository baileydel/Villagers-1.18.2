package com.delke.villagers.villagers.profession;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;

import java.util.Optional;

import static com.delke.villagers.registry.ModVillagers.GUARD_POI;

/**
 * @author Bailey Delker
 * @created 06/13/2023 - 8:51 PM
 * @project Villagers-1.18.2
 */
public class Guard extends NewVillagerProfession {
    public Guard() {
        super("guard", GUARD_POI.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWork() {
        return ImmutableList.of(
                        getMinimalLookBehavior(),
                        Pair.of(5, new RunOne<>(ImmutableList.of(
                                Pair.of(new WorkAtPoi(), 2),
                                Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                                Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                                Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, 0.5f, 1, 6, MemoryModuleType.JOB_SITE), 5)))
                        ),
                        Pair.of(10, new ShowTradesToPlayer(400, 1600)),
                        Pair.of(1, new SetLookAndInteract(EntityType.PLAYER, 1)),
                        Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, 0.5f, 9, 100, 1200)),
                        Pair.of(3, new GiveGiftToHero(100)),
                        Pair.of(99, new UpdateActivityFromSchedule())
                );
    }

    @Override
    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCore() {
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new PoiCompetitorScan(this)),
                Pair.of(5, new GoToWantedItem(0.5f, false, 4)),
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
    public Schedule getSchedule() {
        return new ScheduleBuilder(
                new Schedule())
                .changeActivityAt(0, Activity.WORK)
                .build();
    }

    @Override
    public void registerBrain(Brain<Villager> brain) {
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> core = getCore();
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> work = getWork();

        brain.setSchedule(getSchedule());
        brain.addActivityWithConditions(Activity.WORK, work,
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)
                )
        );

        brain.addActivity(Activity.CORE, core);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
    }

    @Override
    public void registerGoals(Villager villager) {
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.0, true));
        villager.goalSelector.addGoal(2, new MoveTowardsTargetGoal(villager, 0.9, 32.0F));
        villager.goalSelector.addGoal(2, new MoveBackToVillageGoal(villager, 0.6, false));
        villager.goalSelector.addGoal(8, new RandomLookAroundGoal(villager));
        villager.targetSelector.addGoal(2, new HurtByTargetGoal(villager));
        villager.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(villager, Mob.class, 5, false, false,
                (p_28879_) -> p_28879_ instanceof Enemy && !(p_28879_ instanceof Creeper)));
    }

    private static Pair<Integer, Behavior<LivingEntity>> getMinimalLookBehavior() {
        return
                Pair.of(5, new RunOne<>(ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new DoNothing(30, 60), 8)
                )));
    }
}
