package com.delke.villagers.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class VillagerInventoryScreen extends AbstractContainerScreen<VillagerInventoryMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;

    public VillagerInventoryScreen(VillagerInventoryMenu menu, Inventory playerInv) {
        super(menu, playerInv, TextComponent.EMPTY);
        this.passEvents = false;
        this.containerRows = menu.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public void render(@NotNull PoseStack p_98418_, int p_98419_, int p_98420_, float p_98421_) {
        this.renderBackground(p_98418_);
        super.render(p_98418_, p_98419_, p_98420_, p_98421_);
        this.renderTooltip(p_98418_, p_98419_, p_98420_);
    }

    protected void renderBg(@NotNull PoseStack p_98413_, float p_98414_, int p_98415_, int p_98416_) {
       // RenderSystem.setShader(GameRenderer::getPositionTexShader);
       // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
       // RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
       // int $$4 = (this.width - this.imageWidth) / 2;
       // int $$5 = (this.height - this.imageHeight) / 2;
       // this.blit(p_98413_, $$4, $$5, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
       // this.blit(p_98413_, $$4, $$5 + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}