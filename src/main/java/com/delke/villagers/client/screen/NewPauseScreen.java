package com.delke.villagers.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Bailey Delker
 * @created 07/21/2023 - 7:53 AM
 * @project Villagers-1.18.2
 */
public class NewPauseScreen extends Screen {
        private final boolean showPauseMenu;

        public NewPauseScreen(boolean p_96308_) {
            super(p_96308_ ? new TranslatableComponent("menu.game") : new TranslatableComponent("menu.paused"));
            this.showPauseMenu = p_96308_;
        }

        protected void init() {
            if (this.showPauseMenu) {
                this.createPauseMenu();
            }
        }

        private void createPauseMenu() {
            assert this.minecraft != null;

            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20, new TranslatableComponent("menu.returnToGame"), (p_96337_) -> {
                this.minecraft.setScreen(null);
                this.minecraft.mouseHandler.grabMouse();
            }));

            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 48 - 16, 98, 20, new TranslatableComponent("gui.advancements"), (p_96335_) -> {
                assert this.minecraft.player != null;
                this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()));
            }));
            this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 48 - 16, 98, 20, new TranslatableComponent("gui.stats"), (p_96333_) -> {
                assert this.minecraft.player != null;
                this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()));
            }));

            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 96 - 16, 98, 20, new TranslatableComponent("menu.options"), (p_96323_) -> {
                this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
            }));
            Button button = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslatableComponent("menu.shareToLan"), (p_96321_) -> {
                this.minecraft.setScreen(new ShareToLanScreen(this));
            }));
            button.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();

            Component component = this.minecraft.isLocalServer() ? new TranslatableComponent("menu.returnToMenu") : new TranslatableComponent("menu.disconnect");
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 - 16, 204, 20, component, (p_96315_) -> {
                boolean flag = this.minecraft.isLocalServer();
                boolean flag1 = this.minecraft.isConnectedToRealms();
                p_96315_.active = false;
                this.minecraft.level.disconnect();
                if (flag) {
                    this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
                } else {
                    this.minecraft.clearLevel();
                }

                TitleScreen titlescreen = new TitleScreen();
                if (flag) {
                    this.minecraft.setScreen(titlescreen);
                } else if (flag1) {
                    this.minecraft.setScreen(new RealmsMainScreen(titlescreen));
                } else {
                    this.minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
                }

            }));
        }

        public void tick() {
            super.tick();
        }

        public void render(@NotNull PoseStack stack, int p_96311_, int p_96312_, float p_96313_) {
            if (this.showPauseMenu) {
                this.renderBackground(stack);
                drawCenteredString(stack, this.font, this.title, this.width / 2, 40, 16777215);
            } else {
                drawCenteredString(stack, this.font, this.title, this.width / 2, 10, 16777215);
            }

            super.render(stack, p_96311_, p_96312_, p_96313_);
        }
    }