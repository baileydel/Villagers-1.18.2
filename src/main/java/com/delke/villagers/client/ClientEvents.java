package com.delke.villagers.client;

import com.delke.villagers.ExampleMod;
import com.delke.villagers.client.debug.VillagerDebugger;
import com.delke.villagers.client.rendering.NewVillagerModel;
import com.delke.villagers.client.rendering.NewVillagerRenderer;
import com.delke.villagers.client.screen.MainScreen;
import com.delke.villagers.client.screen.NewPauseScreen;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.delke.villagers.client.rendering.NewVillagerModel.*;
import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY;

/**
 * @author Bailey Delker
 * @created 06/21/2023 - 11:00 PM
 * @project Villagers-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    private final Map<Villager, VillagerDebugger> debuggers = new HashMap<>();

    @Nullable
    private List<LevelSummary> cachedList;

    //TODO Extract GUI Mod
    boolean v = false;
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void OverrideMainMenu(ScreenEvent.InitScreenEvent event) {

        // Autoload into world
        if (event.getScreen() instanceof TitleScreen) {
            Minecraft.getInstance().setScreen(new MainScreen());

            LevelStorageSource levelstoragesource = Minecraft.getInstance().getLevelSource();
            if (this.cachedList == null) {
                try {
                    this.cachedList = levelstoragesource.getLevelList();
                }
                catch (LevelStorageException levelstorageexception) {
                    return;
                }

                Collections.sort(this.cachedList);
            }

            if (!this.cachedList.isEmpty() && !v) {
                LevelSummary t = cachedList.get(0);
                loadWorld(t);
                v = true;
            }
        }

        if (event.getScreen() instanceof PauseScreen) {
            Minecraft.getInstance().setScreen(new NewPauseScreen(true));
        }
    }

    private void loadWorld(LevelSummary summary) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EVOKER_CAST_SPELL, 5.0F));
        if (mc.getLevelSource().levelExists(summary.getLevelId())) {
            mc.loadLevel(summary.getLevelId());
        }
    }

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {

        for (Map.Entry<Villager, VillagerDebugger> debuggerEntry : debuggers.entrySet()) {
            debuggerEntry.getValue().searchDebugger.render(event.getPoseStack());
        }

    }

    // Render the current time
    @SubscribeEvent
    public void RenderInfo(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null) {
                PoseStack stack = event.getMatrixStack();
                Window window = event.getWindow();
                Font font = mc.font;

                String s = (player.level.getDayTime() % 24000) + "";
                float w = window.getGuiScaledWidth() / 2F - font.width(s) / 2F;

                font.drawShadow(stack, s, w, 0, 0xFFFFFF);
            }
        }
    }

    @SubscribeEvent
    public void ClientTick(TickEvent.ClientTickEvent event) {
        List<VillagerDebugger> list = debuggers.values().stream().toList();
        for (VillagerDebugger debugger : list) {
            Villager villager = debugger.getVillager();

            if (!villager.isAlive()) {
                debuggers.remove(villager);
            }
        }
    }

    @SubscribeEvent
    public void RenderEntityInfo(RenderNameplateEvent event) {
        try {
            if (event.getEntity() instanceof Villager villager) {
                VillagerDebugger d = debuggers.get(villager);
                if (d != null) {
                    d.renderInfo(event);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void EntitySpawn(EntityJoinWorldEvent event) {
        if (event.getWorld() instanceof ServerLevel) {
            Entity e = event.getEntity();

            if (e instanceof Villager villager) {
                debuggers.putIfAbsent(villager, new VillagerDebugger(villager));
            }
        }
    }

    @Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityType.VILLAGER, NewVillagerRenderer::new);
        }

        @SubscribeEvent
        public static void registerModelLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(MAIN, NewVillagerModel::createBodyLayer);
            event.registerLayerDefinition(INNER_ARMOR, () -> NewVillagerModel.createArmorLayer(new CubeDeformation(0.5F)));
            event.registerLayerDefinition(OUTER_ARMOR, () -> NewVillagerModel.createArmorLayer(new CubeDeformation(1.0F)));
        }
    }
}
