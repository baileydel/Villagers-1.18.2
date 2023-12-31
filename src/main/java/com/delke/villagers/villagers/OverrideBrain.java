package com.delke.villagers.villagers;

import com.delke.villagers.villagers.behavior.ReactToReputation;
import com.delke.villagers.villagers.profession.AbstractProfession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 06/25/2023 - 10:07 PM
 * @project Villagers-1.18.2
 */
public class OverrideBrain {
    // Overrides all villagers AI for this package

    public static void DEFAULT_BRAIN(Brain<Villager> brain, Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();

        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> core = getCorePackage(profession);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> rest = VillagerGoalPackages.getRestPackage(profession, 0.5F);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> idle = getIdlePackage();
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> panic = VillagerGoalPackages.getPanicPackage(profession, 0.5F);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> preraid = VillagerGoalPackages.getPreRaidPackage(profession, 0.5F);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> raid = VillagerGoalPackages.getRaidPackage(profession, 0.5F);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> hide = VillagerGoalPackages.getHidePackage(profession, 0.5F);
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> meet = getMeetPackage();
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> work = VillagerGoalPackages.getWorkPackage(profession, 0.5F);

        Schedule schedule = villager.isBaby() ? Schedule.VILLAGER_BABY : Schedule.VILLAGER_DEFAULT;

        if (profession instanceof AbstractProfession prof) {
            core =      prof.getCorePackage();
            rest =      prof.getRestPackage();
            idle =      prof.getIdlePackage();
            panic =     prof.getPanicPackage();
            preraid =   prof.getPreRaidPackage();
            raid =      prof.getRaidPackage();
            hide =      prof.getHidePackage();
            meet =      prof.getMeetPackage();
            work =      prof.getWorkCorePackage();
            schedule =  prof.getSchedule(villager);
        }

        if (villager.isBaby()) {
            brain.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5F));
        }
        else {
            brain.addActivityWithConditions(Activity.WORK, work, ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));
        }

        brain.setSchedule(schedule);

        brain.addActivity(Activity.CORE, core);

        brain.addActivityWithConditions(Activity.MEET, meet, ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
        brain.addActivity(Activity.REST, rest);
        brain.addActivity(Activity.IDLE, idle);
        brain.addActivity(Activity.PANIC, panic);
        brain.addActivity(Activity.PRE_RAID, preraid);
        brain.addActivity(Activity.RAID, raid);
        brain.addActivity(Activity.HIDE, hide);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(villager.level.getDayTime(), villager.level.getGameTime());
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage(VillagerProfession profession) {
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new VillagerPanicTrigger()),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new ReactToReputation(16F)),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),

                Pair.of(0, new ValidateNearbyPoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(profession.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),

                Pair.of(1, new MoveToTargetSink()),

                Pair.of(2, new PoiCompetitorScan(profession)),
                Pair.of(3, new LookAndFollowTradingPlayerSink(0.5F)),
                Pair.of(5, new GoToWantedItem<>(0.5F, false, 4)),
                Pair.of(6, new AcquirePoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new GoToPotentialJobSite(0.5F)),
                Pair.of(8, new YieldJobSite(0.5F)),
                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))),
                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
                Pair.of(10, new AssignProfessionFromJobSite()),
                Pair.of(10, new ResetProfession())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getIdlePackage() {
        return ImmutableList.of(
                Pair.of(2, new RunOne<>(ImmutableList.of(
                        Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 2),
                        Pair.of(new InteractWith<>(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, 0.5F, 2), 1),
                        Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1),
                        Pair.of(new VillageBoundRandomStroll(0.5F), 1),
                        Pair.of(new SetWalkTargetFromLookTarget(0.5F, 2), 1),
                        Pair.of(new JumpOnBed(0.5F), 1), Pair.of(new DoNothing(30, 60), 1)))
                ),
                Pair.of(3, new GiveGiftToHero(100)), Pair.of(3, new SetLookAndInteract(EntityType.PLAYER, 4)),
                /*Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new TradeWithVillager(), 1))
                        )
                ),*/
                Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new VillagerMakeLove(), 1))
                        )
                ),
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getMeetPackage() {
        return ImmutableList.of(
                Pair.of(2, new RunOne<>(
                        ImmutableList.of(
                                Pair.of(new StrollAroundPoi(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                                Pair.of(new SocializeAtBell(), 2))
                        )
                ),
                Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)),
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, 0.5F, 6, 100, 200)),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new ValidateNearbyPoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT)),
                /*Pair.of(3, new GateBehavior<>(
                        ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        //ImmutableList.of(Pair.of(new TradeWithVillager(), 1))
                        )
                ),*/
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    private static Pair<Integer, Behavior<LivingEntity>> getFullLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.CAT, 8.0F), 8), Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2), Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2), Pair.of(new SetEntityLookTarget(MobCategory.CREATURE, 8.0F), 1), Pair.of(new SetEntityLookTarget(MobCategory.WATER_CREATURE, 8.0F), 1), Pair.of(new SetEntityLookTarget(MobCategory.AXOLOTLS, 8.0F), 1), Pair.of(new SetEntityLookTarget(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1), Pair.of(new SetEntityLookTarget(MobCategory.WATER_AMBIENT, 8.0F), 1), Pair.of(new SetEntityLookTarget(MobCategory.MONSTER, 8.0F), 1), Pair.of(new DoNothing(30, 60), 2))));
    }
}
