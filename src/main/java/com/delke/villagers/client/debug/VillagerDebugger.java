package com.delke.villagers.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.client.event.RenderNameplateEvent;

import java.util.*;

/**
 * @author Bailey Delker
 * @created 03/30/2023 - 6:53 PM
 * @project forge-1.18.2-40.2.1-mdk
 */

@SuppressWarnings({"deprecation"})
public class VillagerDebugger {
    private final Villager villager;
    private final UUID uuid;

    public VillagerDebugger(Villager villager) {
        this.villager = villager;
        this.uuid = villager.getUUID();
    }

    public void render(RenderNameplateEvent event) {
        if (villager.isAlive()) {
            PoseStack stack = event.getPoseStack();
            MultiBufferSource source = event.getMultiBufferSource();

            stack.pushPose();
            stack.translate(0.0D, villager.getBbHeight() + 0.5F, 0.0D);
            stack.mulPose(event.getEntityRenderer().entityRenderDispatcher.cameraOrientation());
            stack.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = stack.last().pose();
            Font font = Minecraft.getInstance().font;
            Brain<Villager> brain = villager.getBrain();

            float y = 17;

            List<String> list = new ArrayList<>();
            for (Behavior<? super Villager> behavior : brain.getRunningBehaviors()) {
                if (brain.getRunningBehaviors().size() > 0 && behavior.toString().contains("DoNothing")) {
                    continue;
                }

                String[] ar = behavior.toString().split(": ");
                String text = ar[ar.length - 1].replace("[", "").replace("]", "");

                if (text.length() > 0 && !list.contains(text)) {
                    list.add(text);

                    font.drawInBatch(new TranslatableComponent(text), (-font.width(text) / 2F), y - 12, -1, false, matrix4f, source, false, 1056964608, 0xFFFFFF);
                    y -= font.lineHeight + 2;
                }
            }

            y = 17;

            for (Activity activity : brain.getActiveActivities().stream().toList()) {
                String an = activity.getName();
                font.drawInBatch(new TranslatableComponent(an), (-font.width(an) / 2F) - 30, y, -1, false, matrix4f, source, false, 1056964608, 0xFFFFFF);
                y += font.lineHeight + 2;
            }
            stack.popPose();
        }
    }


    private UUID getUUID() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VillagerDebugger) {
            return this.uuid.equals(((VillagerDebugger) obj).getUUID());
        }
        return false;
    }

    public Villager getVillager() {
        return this.villager;
    }
}
