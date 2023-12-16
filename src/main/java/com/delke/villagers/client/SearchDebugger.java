package com.delke.villagers.client;

import com.delke.villagers.client.rendering.RenderingUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/25/2023 - 3:16 PM
 * @project Villagers-1.18.2
 */
public class SearchDebugger {
    @Nullable
    private BoundingBox area;
    private List<BlockPos> found = new ArrayList<>();

    public SearchDebugger() {
        this(null);
    }

    public SearchDebugger(@Nullable BoundingBox area) {
        this.area = area;
    }

    public void render(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level != null && area != null) {

            RenderingUtil.renderBoundingBox(stack, area);

            for (BlockPos pos : found) {
                BlockState blockstate = level.getBlockState(pos);

                Camera camera = mc.gameRenderer.getMainCamera();
                Vec3 vec3 = camera.getPosition();
                double d0 = vec3.x();
                double d1 = vec3.y();
                double d2 = vec3.z();

                if (!blockstate.isAir() && level.getWorldBorder().isWithinBounds(pos)) {
                    RenderingUtil.renderHitOutline(stack, mc.renderBuffers().bufferSource().getBuffer(RenderType.lines()), camera.getEntity(), d0, d1, d2, pos, blockstate);
                }
            }
        }
    }

    public void setArea(@Nullable BoundingBox area) {
        this.area = area;
    }

    public void addFound(BlockPos pos) {
        if (!found.contains(pos)) {
            found.add(pos);
        }
    }

    public void clear() {
        this.area = null;
        this.found = new ArrayList<>();
    }
}
