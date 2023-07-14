package com.delke.villagers.villagers.profession;

import com.delke.villagers.villagers.OverrideBrain;
import com.delke.villagers.villagers.behavior.Produce;
import com.delke.villagers.villagers.behavior.ReactToReputation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 06/18/2023 - 12:21 PM
 * @project Villagers-1.18.2
 */
public class AbstractProfession extends VillagerProfession {

    public AbstractProfession(String name, PoiType block, ImmutableSet<Item> request, ImmutableSet<Block> pois, @Nullable SoundEvent workSound) {
        super(name, block, request, pois, workSound);
    }

    public void registerBrain(Brain<Villager> brain, Villager villager) {
        OverrideBrain.DEFAULT_BRAIN(brain, villager);
    }

    public void registerGoals(Villager villager) {}

    public Schedule getSchedule(Villager villager) {
        return villager.isBaby() ? Schedule.VILLAGER_BABY : Schedule.VILLAGER_DEFAULT;
    }

    public ImmutableList<ItemStack> getProducibleItems() {
        return ImmutableList.of();
    }

    public boolean isProducer() {
        return false;
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getWorkCorePackage() {
        List<Pair<Integer, ? extends Behavior<? super Villager>>> t = new ArrayList<Pair<Integer, ? extends Behavior<? super Villager>>>(List.of(
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.JOB_SITE, 0.5F, 9, 100, 1200)),

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

    /**
     * Gets POI Behaviors, used my getRunOne
     * Is added to the list of Single executed work behaviors
     * @return
     */
    public List<Pair<Behavior<? super Villager>, Integer>> getWorkPOIBehaviorPackage() {
        return new ArrayList<>(List.of(
                getWorkPOIBehavior(),
                Pair.of(new StrollAroundPoi(MemoryModuleType.JOB_SITE, 0.4F, 4), 6),
                Pair.of(new StrollToPoiList(MemoryModuleType.SECONDARY_JOB_SITE, 0.5F, 1, 6, MemoryModuleType.JOB_SITE), 9)
        ));
    }


    /**
     * Gets work poi behavior (most of it is looking at a block)
     * 
     * @return
     */
    public Pair<Behavior<? super Villager>, Integer> getWorkPOIBehavior() {
        return Pair.of(new WorkAtPoi(), 7);
    }

    /**
     * Creates a list of behaviors for coreWorkPackage
     * Only one of these behaviors will be executed a time
     * @return
     */
    public List<Pair<Behavior<? super Villager>, Integer>> getRunOnePackage() {
        //TODO make this more advanced

        // Every Villager will go to their work site.
        List<Pair<Behavior<? super Villager>, Integer>> t = new ArrayList<>(List.of(
                Pair.of(new StrollToPoi(MemoryModuleType.JOB_SITE, 0.4F, 2, 10), 5)
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

    public List<Pair<Behavior<? super Villager>, Integer>> getSecondWorkPackage() {
        return List.of();
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new ReactToReputation(16F)),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new VillagerPanicTrigger()),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new PoiCompetitorScan(this)),
                Pair.of(3, new LookAndFollowTradingPlayerSink(0.5F)),
                Pair.of(5, new GoToWantedItem<>(0.5F, false, 4)),
                Pair.of(6, new AcquirePoi(getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new GoToPotentialJobSite(0.5F)),
                Pair.of(8, new YieldJobSite(0.5F)),
                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte)14))),
                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))),
                Pair.of(10, new AssignProfessionFromJobSite()),
                Pair.of(10, new ResetProfession())
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getRestPackage() {
        return ImmutableList.of(
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.HOME, 0.5F, 1, 150, 1200)),
                Pair.of(3, new ValidateNearbyPoi(PoiType.HOME, MemoryModuleType.HOME)),
                Pair.of(3, new SleepInBed()),
                Pair.of(5, new RunOne<>(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT),
                        ImmutableList.of(
                                Pair.of(new SetClosestHomeAsWalkTarget(0.5F), 1),
                                Pair.of(new InsideBrownianWalk(0.5F), 4),
                                Pair.of(new GoToClosestVillage(0.5F, 4), 2),
                                Pair.of(new DoNothing(20, 40), 2))
                        )
                ),
                getMinimalLookBehavior(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getMeetPackage() {
        return ImmutableList.of(
                Pair.of(2, new RunOne<>(ImmutableList.of(
                        Pair.of(new StrollAroundPoi(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2),
                        Pair.of(new SocializeAtBell(), 2)))
                ),
                Pair.of(10, new SetLookAndInteract(EntityType.PLAYER, 4)),
                Pair.of(2, new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, 0.5F, 6, 100, 200)),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new ValidateNearbyPoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT)),
                Pair.of(3, new GateBehavior<>(ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(/*Pair.of(new TradeWithVillager(), 1)*/))

                ),
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getIdlePackage() {
        return ImmutableList.of(
                Pair.of(2, new RunOne<>(ImmutableList.of(
                        Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 2),
                        Pair.of(new InteractWith<>(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, 0.5F, 2), 1),
                        Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1),
                        Pair.of(new VillageBoundRandomStroll(0.5F), 1),
                        Pair.of(new SetWalkTargetFromLookTarget(0.5F, 2), 1),
                        Pair.of(new JumpOnBed(0.5F), 1), Pair.of(new DoNothing(30, 60), 1)))
                ),
                Pair.of(3, new GiveGiftToHero(100)),
                Pair.of(3, new SetLookAndInteract(EntityType.PLAYER, 4)),
                Pair.of(3, new GateBehavior<>(ImmutableMap.of(),
                        ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))
                ),
                Pair.of(3,
                        new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(Pair.of(new VillagerMakeLove(), 1)))),
                getFullLookBehavior(),
                Pair.of(99, new UpdateActivityFromSchedule())
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPanicPackage() {
        float f = 0.5F * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new VillagerCalmDown()),
                Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)),
                Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                Pair.of(3, new VillageBoundRandomStroll(f, 2, 2)),
                getMinimalLookBehavior()
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPreRaidPackage() {
        return ImmutableList.of(
                Pair.of(0, new RingBell()),
                Pair.of(0, new RunOne<>(ImmutableList.of(
                        Pair.of(new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, 0.5F * 1.5F, 2, 150, 200), 6),
                        Pair.of(new VillageBoundRandomStroll(0.5F * 1.5F), 2)))
                ),
                getMinimalLookBehavior(),
                Pair.of(99, new ResetRaidStatus())
        );
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getRaidPackage() {
        return ImmutableList.of(
                Pair.of(0, new RunOne<>(ImmutableList.of(
                        Pair.of(new GoOutsideToCelebrate(0.5F), 5),
                        Pair.of(new VictoryStroll(0.5F * 1.1F), 2)))
                ),
                Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)),
                Pair.of(2, new LocateHidingPlaceDuringRaid(24, 0.5F * 1.4F)),
                getMinimalLookBehavior(),
                Pair.of(99, new ResetRaidStatus()));
    }

    public ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getHidePackage() {
        return ImmutableList.of(
                Pair.of(0, new SetHiddenState(15, 3)),
                Pair.of(1, new LocateHidingPlace(32, 0.5F * 1.25F, 2)),
                getMinimalLookBehavior());
    }

    public Pair<Integer, Behavior<LivingEntity>> getFullLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.CAT, 8.0F), 8),
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new SetEntityLookTarget(MobCategory.CREATURE, 8.0F), 1),
                        Pair.of(new SetEntityLookTarget(MobCategory.WATER_CREATURE, 8.0F), 1),
                        Pair.of(new SetEntityLookTarget(MobCategory.AXOLOTLS, 8.0F), 1),
                        Pair.of(new SetEntityLookTarget(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1),
                        Pair.of(new SetEntityLookTarget(MobCategory.WATER_AMBIENT, 8.0F), 1),
                        Pair.of(new SetEntityLookTarget(MobCategory.MONSTER, 8.0F), 1),
                        Pair.of(new DoNothing(30, 60), 2))
                )
        );
    }

    protected Pair<Integer, Behavior<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2),
                        //Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new DoNothing(30, 60), 8))
                )
        );
    }
}
