package net.cozystudios.tokimiboatyard.screen;

import net.cozystudios.tokimiboatyard.registry.ModScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UpgradedBoatScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final int rows;

    //? if <1.21.1 {
    public UpgradedBoatScreenHandler(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        this(syncId, playerInv, buf.readByte() & 0xFF);
    }
    //? }

    public UpgradedBoatScreenHandler(int syncId, Inventory playerInv, int rows) {
        this(syncId, playerInv, new SimpleContainer(rows * 9), rows);
    }

    public UpgradedBoatScreenHandler(int syncId, Inventory playerInv, Container inventory, int rows) {
        super(ModScreens.UPGRADED_BOAT, syncId);
        checkContainerSize(inventory, rows * 9);
        this.inventory = inventory;
        this.rows = rows;
        inventory.startOpen(playerInv.player);

        int extraOffset = (rows - 4) * 18;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inventory, c + r * 9, 8 + c * 18, 18 + r * 18));
            }
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(playerInv, c + r * 9 + 9, 8 + c * 18, 103 + r * 18 + extraOffset));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(playerInv, c, 8 + c * 18, 161 + extraOffset));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack src = slot.getItem();
            result = src.copy();
            int boatEnd = rows * 9;
            if (slotIndex < boatEnd) {
                if (!this.moveItemStackTo(src, boatEnd, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(src, 0, boatEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (src.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    public int getRows() { return rows; }
    public Container getInventory() { return inventory; }
}
