package com.delke.villagers.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ModListScreen;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class MainScreen extends Screen {
   public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
   private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
   private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");

   private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
   private final boolean minceraftEasterEgg;
   private final boolean fading;
   private long fadeInStart;

   @Nullable
   private MainScreen.Warning32Bit warning32Bit;

   public MainScreen() {
      this(false);
   }

   public MainScreen(boolean p_96733_) {
      super(new TranslatableComponent("narrator.screen.title"));
      this.fading = p_96733_;
      this.minceraftEasterEgg = (double)(new Random()).nextFloat() < 1.0E-4D;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      assert this.minecraft != null;

      int l = this.height / 4 + 48;
      this.createNormalMenuOptions(l);

      this.addRenderableWidget(new Button(this.width / 2 - 100, l + 24 * 2, 98, 20, new TranslatableComponent("fml.menu.mods"),
              (button) -> this.minecraft.setScreen(new ModListScreen(this))));

      this.addRenderableWidget(new Button(this.width / 2 + 2, l + 24 * 2, 98, 20, new TranslatableComponent("menu.options"),
              (button) -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));

      this.addRenderableWidget(new Button(this.width / 2 - 49, l + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"),
              (button) -> this.minecraft.stop()));
   }

   private void createNormalMenuOptions(int p_96764_) {
      assert this.minecraft != null;
      this.addRenderableWidget(
              new Button(this.width / 2 - 100, p_96764_, 200, 20, new TranslatableComponent("menu.singleplayer"), (p_96776_) -> this.minecraft.setScreen(new SelectWorldScreen(this))));

      boolean flag = this.minecraft.allowsMultiplayer();
      Button.OnTooltip button$ontooltip = flag ? Button.NO_TOOLTIP : new Button.OnTooltip() {
         private final Component text = new TranslatableComponent("title.multiplayer.disabled");

         public void onTooltip(@NotNull Button p_169458_, @NotNull PoseStack p_169459_, int p_169460_, int p_169461_) {
            if (!p_169458_.active) {
               MainScreen.this.renderTooltip(p_169459_, MainScreen.this.minecraft.font.split(this.text, Math.max(MainScreen.this.width / 2 - 43, 170)), p_169460_, p_169461_);
            }
         }

         public void narrateTooltip(@NotNull Consumer<Component> p_169456_) {
            p_169456_.accept(this.text);
         }
      };
      (this.addRenderableWidget(new Button(this.width / 2 - 100, p_96764_ + 24, 200, 20, new TranslatableComponent("menu.multiplayer"), (p_210872_) -> {
         Screen screen = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
         this.minecraft.setScreen(screen);
      }, button$ontooltip))).active = flag;
   }


   public void render(@NotNull PoseStack p_96739_, int p_96740_, int p_96741_, float p_96742_) {
      if (this.fadeInStart == 0L && this.fading) {
         this.fadeInStart = Util.getMillis();
      }

      float f = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
      this.panorama.render(p_96742_, Mth.clamp(f, 0.0F, 1.0F));
      int j = this.width / 2 - 137;
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
      blit(p_96739_, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
      float f1 = this.fading ? Mth.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
      int l = Mth.ceil(f1 * 255.0F) << 24;
      if ((l & -67108864) != 0) {
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);
         if (this.minceraftEasterEgg) {
            this.blitOutlineBlack(j, 30, (p_210862_, p_210863_) -> {
               this.blit(p_96739_, p_210862_, p_210863_, 0, 0, 99, 44);
               this.blit(p_96739_, p_210862_ + 99, p_210863_, 129, 0, 27, 44);
               this.blit(p_96739_, p_210862_ + 99 + 26, p_210863_, 126, 0, 3, 44);
               this.blit(p_96739_, p_210862_ + 99 + 26 + 3, p_210863_, 99, 0, 26, 44);
               this.blit(p_96739_, p_210862_ + 155, p_210863_, 0, 45, 155, 44);
            });
         }
         else {
            this.blitOutlineBlack(j, 30, (p_211778_, p_211779_) -> {
               this.blit(p_96739_, p_211778_, p_211779_, 0, 0, 155, 44);
               this.blit(p_96739_, p_211778_ + 155, p_211779_, 0, 45, 155, 44);
            });
         }

         RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
         blit(p_96739_, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
         if (this.warning32Bit != null) {
            this.warning32Bit.label.renderBackgroundCentered(p_96739_, this.warning32Bit.x, this.warning32Bit.y, 9, 2, 1428160512);
            this.warning32Bit.label.renderCentered(p_96739_, this.warning32Bit.x, this.warning32Bit.y, 9, 16777215 | l);
         }

         assert this.minecraft != null;

         Minecraft.checkModStatus();


         for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener instanceof AbstractWidget) {
               ((AbstractWidget)guieventlistener).setAlpha(f1);
            }
         }

         super.render(p_96739_, p_96740_, p_96741_, p_96742_);
      }
   }

   public boolean mouseClicked(double p_96735_, double p_96736_, int p_96737_) {
      return super.mouseClicked(p_96735_, p_96736_, p_96737_);
   }

   @OnlyIn(Dist.CLIENT)
   record Warning32Bit(MultiLineLabel label, int x, int y, CompletableFuture<Boolean> realmsSubscriptionFuture) {
   }
}
