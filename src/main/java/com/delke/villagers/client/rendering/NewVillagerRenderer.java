package com.delke.villagers.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static com.delke.villagers.client.rendering.NewVillagerModel.*;

@OnlyIn(Dist.CLIENT)
public class NewVillagerRenderer extends HumanoidMobRenderer<Villager, NewVillagerModel<Villager>> {
   private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("villagers", "textures/entity/villager/villager.png");

   public NewVillagerRenderer(EntityRendererProvider.Context context) {
      super(context, new NewVillagerModel<>(context.bakeLayer(MAIN)), 0.5F);
      this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
      this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
      //Armor layer
      this.addLayer(
              new HumanoidArmorLayer<>(this,
              new NewVillagerModel<>(context.bakeLayer(INNER_ARMOR)),
              new NewVillagerModel<>(context.bakeLayer(OUTER_ARMOR)))
      );
   }

   public @NotNull ResourceLocation getTextureLocation(@NotNull Villager p_116312_) {
      return VILLAGER_BASE_SKIN;
   }

   protected void scale(Villager villager, @NotNull PoseStack stack, float p_116316_) {
      float f = 0.9375F;
      if (villager.isBaby()) {
         f *= 0.5F;
         this.shadowRadius = 0.25F;
      } else {
         this.shadowRadius = 0.5F;
      }

      stack.scale(f, f, f);
   }
}