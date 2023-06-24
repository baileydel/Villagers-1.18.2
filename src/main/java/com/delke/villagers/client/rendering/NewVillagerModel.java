package com.delke.villagers.client.rendering;

import com.delke.villagers.ExampleMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class NewVillagerModel<T extends Villager> extends HumanoidModel<T> implements VillagerHeadModel {
   public static final ModelLayerLocation MAIN = new ModelLayerLocation(new ResourceLocation(ExampleMod.MOD_ID, "newvillager"), "main");
   public static final ModelLayerLocation OUTER_ARMOR = new ModelLayerLocation(new ResourceLocation(ExampleMod.MOD_ID, "villager_armor"), "outer_armor");
   public static final ModelLayerLocation INNER_ARMOR = new ModelLayerLocation(new ResourceLocation(ExampleMod.MOD_ID, "villager_armor"), "inner_armor");

   private final ModelPart hatRim = this.hat.getChild("hat_rim");

   public NewVillagerModel(ModelPart p_171092_) {
      super(p_171092_);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();

      partdefinition.addOrReplaceChild("head", (new CubeListBuilder()).texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.ZERO);
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
              .texOffs(32, 0)
              .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F,
                      new CubeDeformation(0.5F)), PartPose.ZERO);

      partdefinition1.addOrReplaceChild("hat_rim", CubeListBuilder.create()
              .texOffs(30, 47)
              .addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F),
              PartPose.rotation((-(float)Math.PI / 2F), 0.0F, 0.0F));

      partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
              .texOffs(16, 20)
              .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
              .texOffs(0, 38)
              .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F,
                      new CubeDeformation(0.05F)), PartPose.ZERO);

      partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 22).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createArmorLayer(CubeDeformation p_171094_) {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(p_171094_, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_171094_), PartPose.ZERO);
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, p_171094_.extend(0.1F)), PartPose.ZERO);
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_171094_.extend(0.1F)), PartPose.offset(-2.0F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_171094_.extend(0.1F)), PartPose.offset(2.0F, 12.0F, 0.0F));
      partdefinition.getChild("hat").addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(@NotNull T villager, float p_104054_, float p_104055_, float p_104056_, float p_104057_, float p_104058_) {
      super.setupAnim(villager, p_104054_, p_104055_, p_104056_, p_104057_, p_104058_);

      boolean $$6 = villager.getUnhappyCounter() > 0;

      this.head.yRot = p_104057_ * 0.017453292F;
      this.head.xRot = p_104058_ * 0.017453292F;
      if ($$6) {
         this.head.zRot = 0.3F * Mth.sin(0.45F * p_104056_);
         this.head.xRot = 0.4F;
      } else {
         this.head.zRot = 0.0F;
      }

      this.rightLeg.xRot = Mth.cos(p_104054_ * 0.6662F) * 1.4F * p_104055_ * 0.5F;
      this.leftLeg.xRot = Mth.cos(p_104054_ * 0.6662F + 3.1415927F) * 1.4F * p_104055_ * 0.5F;
      this.rightLeg.yRot = 0.0F;
      this.leftLeg.yRot = 0.0F;
      //AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, villager.isAggressive(), this.attackTime, p_104178_);
   }

   public void hatVisible(boolean p_104182_) {
      this.head.visible = p_104182_;
      this.hat.visible = p_104182_;
      this.hatRim.visible = p_104182_;
   }
}