package com.delke.villagers.villagers.profession;

import com.delke.villagers.villagers.behavior.TillFarmland;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 06/17/2023 - 11:50 PM
 * @project Villagers-1.18.2
 */
public class Farmer extends NewVillagerProfession {

    public Farmer(String p_35607_, PoiType p_35608_, ImmutableSet<Item> p_35609_, ImmutableSet<Block> p_35610_, @Nullable SoundEvent p_35611_) {
        super(p_35607_, p_35608_, p_35609_, p_35610_, p_35611_);
    }

    @Override
    ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWork() {
        return null;
    }

    @Override
    ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCore() {
        return null;
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWork(VillagerProfession profession, float p_24591_) {
        WorkAtPoi workatpoi;
        if (profession == VillagerProfession.FARMER) {
            workatpoi = new WorkAtComposter();
        } else {
            workatpoi = new WorkAtPoi();
        }

        return
                ImmutableList.of(
                        getMinimalLookBehavior(),
                        Pair.of(5, new RunOne<>(ImmutableList.of(
                                Pair.of(workatpoi, 2),
                                Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4F, 4), 2),
                                Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5),
                                Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, p_24591_, 1, 6, MemoryModuleType.JOB_SITE), 5),
                                Pair.of(new TillFarmland(), profession == VillagerProfession.FARMER ? 3 : 5),
                                Pair.of(new HarvestFarmland(), profession == VillagerProfession.FARMER ? 4 : 5),
                                Pair.of(new UseBonemeal(), profession == VillagerProfession.FARMER ? 5 : 7)))
                        ),
                        Pair.of(1, new SetLookAndInteract(EntityType.PLAYER, 1)),
                        Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, p_24591_, 9, 100, 1200)),
                        Pair.of(99, new UpdateActivityFromSchedule())
                );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCore(VillagerProfession p_24586_, float p_24587_) {
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new VillagerPanicTrigger()),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),
                Pair.of(0, new ValidateNearbyPoi(p_24586_.getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(p_24586_.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetSink()), Pair.of(2, new PoiCompetitorScan(p_24586_)),
                Pair.of(3, new LookAndFollowTradingPlayerSink(p_24587_)),
                Pair.of(5, new GoToWantedItem(p_24587_, false, 4)),
                Pair.of(6, new AcquirePoi(p_24586_.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new GoToPotentialJobSite(p_24587_)),
                Pair.of(8, new YieldJobSite(p_24587_)),
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

    }

    @Override
    public void registerGoals(Villager villager) {

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
