package com.delke.villagers.client.debug;

import com.delke.villagers.client.rendering.RenderingUtil;
import com.delke.villagers.villages.Building;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildingRenderer {

    private static BoundingBox area;

    public static final Building lumberhouse = new Building();

    public static void render(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        area = new BoundingBox(-7,-60, -7, -2, -56, 1);

        if (level != null) {

            RenderingUtil.renderBoundingBox(stack, area);
        }
    }
}