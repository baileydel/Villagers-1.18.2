package com.delke.villagers.client;

import com.delke.villagers.ExampleMod;
import com.delke.villagers.client.debug.BuildingRenderer;
import com.delke.villagers.client.debug.VillagerDebugger;
import com.delke.villagers.client.rendering.NewVillagerModel;
import com.delke.villagers.client.rendering.NewVillagerRenderer;
import com.delke.villagers.villagers.VillagerUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.text.html.HTML;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.delke.villagers.client.rendering.NewVillagerModel.*;

/**
 * @author Bailey Delker
 * @created 06/21/2023 - 11:00 PM
 * @project Villagers-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static final Map<Villager, VillagerDebugger> debuggers = new HashMap<>();

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)) {
            //render building position of class houses
            BuildingRenderer.render(event.getPoseStack());
            for (Map.Entry<Villager, VillagerDebugger> debuggerEntry : debuggers.entrySet()) {
                debuggerEntry.getValue().searchDebugger.render(event.getPoseStack());
            }
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
