package com.delke.villagers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/25/2023 - 3:16 PM
 * @project Villagers-1.18.2
 */
public class SearchDebugger {
    private List<BlockPos> found = new ArrayList<>();
    private BoundingBox area;
    private List<BlockState> searchFor;
    private final Level level;

    public SearchDebugger() {
        this(List.of(), null);
    }

    public SearchDebugger(List<BlockState> searchFor, BoundingBox area) {
        this.area = area;
        this.searchFor = searchFor;
        this.level = Minecraft.getInstance().level;
    }

    public void render(PoseStack stack) {

        if (Minecraft.getInstance().player != null) {
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vertexconsumer = multibuffersource$buffersource.getBuffer(RenderType.lines());

            BlockPos pos = new BlockPos(0, -60, 0);
            BlockPos pos2 = pos.offset(4, 4, 4);

            area = new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());

            double d0 = ((float)(pos2.getX() - pos.getX()) + 0.45F);
            double d1 = ((float)(pos2.getY() - pos.getY()) + 0.45F);
            double d2 = (float)(pos2.getZ() - pos.getZ()) + 0.45F;
            double d3 = ((float)(pos2.getX() - pos.getX()) + 0.55F);
            double d4 = ((float)(pos2.getY() - pos.getY()) + 0.55F);
            double d5 = ((float)(pos2.getZ() - pos.getZ()) + 0.55F);

            //TODO Render outline of area we are searching in

            LevelRenderer.renderLineBox(stack, vertexconsumer, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
        /*
        // This is rendering outlines of a block, we only want to do this when a block is found
        HitResult hitresult = Minecraft.getInstance().hitResult;
        if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos1 = ((BlockHitResult) hitresult).getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos1);

            Vec3 vec3 = camera.getPosition();
            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();

            if (!blockstate.isAir() && this.level.getWorldBorder().isWithinBounds(blockpos1)) {
                renderHitOutline(stack, vertexconsumer, camera.getEntity(), d0, d1, d2, blockpos1, blockstate);
            }
        }
         */

        }
    }

    private void renderHitOutline(PoseStack stack, VertexConsumer vertexConsumer, Entity entity, double p_109641_, double p_109642_, double p_109643_, BlockPos p_109644_, BlockState p_109645_) {
        renderShape(stack, vertexConsumer, p_109645_.getShape(this.level, p_109644_, CollisionContext.of(entity)), (double)p_109644_.getX() - p_109641_, (double)p_109644_.getY() - p_109642_, (double)p_109644_.getZ() - p_109643_, 0.0F, 0.0F, 0.0F, 0.4F);
    }

    private void renderShape(PoseStack stack, VertexConsumer vertexConsumer, VoxelShape shape, double p_109786_, double p_109787_, double p_109788_, float p_109789_, float p_109790_, float p_109791_, float p_109792_) {
        PoseStack.Pose posestack$pose = stack.last();
        shape.forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
            float f = (float)(p_194327_ - p_194324_);
            float f1 = (float)(p_194328_ - p_194325_);
            float f2 = (float)(p_194329_ - p_194326_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            vertexConsumer.vertex(posestack$pose.pose(), (float)(p_194324_ + p_109786_), (float)(p_194325_ + p_109787_), (float)(p_194326_ + p_109788_)).color(p_109789_, p_109790_, p_109791_, p_109792_).normal(posestack$pose.normal(), f, f1, f2).endVertex();
            vertexConsumer.vertex(posestack$pose.pose(), (float)(p_194327_ + p_109786_), (float)(p_194328_ + p_109787_), (float)(p_194329_ + p_109788_)).color(p_109789_, p_109790_, p_109791_, p_109792_).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
    }

    public void searchFor(BlockState state, BoundingBox area) {

    }

    public void clear() {
        this.found = new ArrayList<>();
    }

}
