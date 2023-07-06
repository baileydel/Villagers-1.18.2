package com.delke.villagers.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static net.minecraft.client.gui.GuiComponent.blit;
import static net.minecraft.client.gui.GuiComponent.fill;

/**
 * @author Bailey Delker
 * @created 07/01/2023 - 11:46 PM
 * @project Villagers-1.18.2
 */

//TODO Extract GUI Mod
@Mixin(LoadingOverlay.class)
public abstract class OverlayMixin {
    @Shadow private long fadeOutStart;

    @Shadow @Final private boolean fadeIn;

    @Shadow private long fadeInStart;

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    private static int replaceAlpha(int p_169325_, int p_169326_) {
        return p_169325_ & 16777215 | p_169326_ << 24;
    }

    @Shadow @Final private ReloadInstance reload;

    @Shadow @Final
    static ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION;

    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setProfession(PoseStack p_96178_, int p_96179_, int p_96180_, float p_96181_, CallbackInfo ci) {
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        int j = this.minecraft.getWindow().getGuiScaledHeight();
        int BRAND_BACKGROUND = FastColor.ARGB32.color(255, 0, 0, 0);

        long k = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = k;
        }

        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
        float f2;

        if (f >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(p_96178_, 0, 0, p_96181_);
            }

            int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(p_96178_, 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND, l));
            f2 = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        }
        else if (this.fadeIn) {
            if (this.minecraft.screen != null && f1 < 1.0F) {
                this.minecraft.screen.render(p_96178_, p_96179_, p_96180_, p_96181_);
            }

            int l1 = Mth.ceil(Mth.clamp(f1, 0.15D, 1.0D) * 255.0D);
            fill(p_96178_, 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND, l1));
            f2 = Mth.clamp(f1, 0.0F, 1.0F);
        } else {
            float f3 = (float)(BRAND_BACKGROUND >> 16 & 255) / 255.0F;
            float f4 = (float)(BRAND_BACKGROUND >> 8 & 255) / 255.0F;
            float f5 = (float)(BRAND_BACKGROUND & 255) / 255.0F;
            GlStateManager._clearColor(f3, f4, f5, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            f2 = 1.0F;
        }

        int j2 = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int k2 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5D);
        double d1 = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int i1 = (int)(d1 * 0.5D);
        double d0 = d1 * 4.0D;
        int j1 = (int)(d0 * 0.5D);

        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f2);

        blit(p_96178_, j2 - j1, k2 - i1, j1, (int)d1, -0.0625F, 0.0F, 120, 60, 120, 120);
        blit(p_96178_, j2, k2 - i1, j1, (int)d1, 0.0625F, 60.0F, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        if (f >= 2.0F) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f1 >= 2.0F)) {
            this.fadeOutStart = Util.getMillis(); // Moved up to guard against inf loops caused by callback
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
        ci.cancel();
    }
}
