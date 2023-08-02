package com.delke.villagers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VillagerInventoryMenu extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows = 1;

    public VillagerInventoryMenu(int id, Inventory playerInv, Container container) {
        super(null, id);

        this.container = container;
        container.startOpen(playerInv.player);

        for (int slots = 0; slots < 9; ++slots) {
            this.addSlot(new Slot(container, slots, 8 + slots * 18, 18));
        }

        for (int rows = 0; rows < 3; ++rows) {
            for(int slots = 0; slots < 9; ++slots) {
                this.addSlot(new Slot(playerInv, slots + rows * 9 + 9, 8 + slots * 18, 102 + rows * 18 - 18));
            }
        }

        for (int rows = 0; rows < 9; ++rows) {
            this.addSlot(new Slot(playerInv, rows, 8 + rows * 18, 142));
        }
    }

    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public @NotNull ItemStack quickMoveStack(@NotNull Player p_39253_, int p_39254_) {
        Slot slot = this.slots.get(p_39254_);

        if (slot.hasItem()) {
            ItemStack $$4 = slot.getItem();
            if (p_39254_ < this.containerRows * 9) {
                if (!this.moveItemStackTo($$4, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo($$4, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if ($$4.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            slot.setChanged();
            return $$4;
        }

        return ItemStack.EMPTY;
    }

    public void removed(@NotNull Player p_39251_) {
        super.removed(p_39251_);
        this.container.stopOpen(p_39251_);
    }

    public int getRowCount() {
        return this.containerRows;
    }
}
