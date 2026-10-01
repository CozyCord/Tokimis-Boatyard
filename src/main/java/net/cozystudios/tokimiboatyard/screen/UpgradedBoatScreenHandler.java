package net.cozystudios.tokimiboatyard.screen;

import net.cozystudios.tokimiboatyard.registry.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class UpgradedBoatScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final int rows;

    public UpgradedBoatScreenHandler(int syncId, PlayerInventory playerInv, PacketByteBuf buf) {
        this(syncId, playerInv, buf.readByte() & 0xFF);
    }

    public UpgradedBoatScreenHandler(int syncId, PlayerInventory playerInv, int rows) {
        this(syncId, playerInv, new SimpleInventory(rows * 9), rows);
    }

    public UpgradedBoatScreenHandler(int syncId, PlayerInventory playerInv, Inventory inventory, int rows) {
        super(ModScreens.UPGRADED_BOAT, syncId);
        checkSize(inventory, rows * 9);
        this.inventory = inventory;
        this.rows = rows;
        inventory.onOpen(playerInv.player);

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
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack src = slot.getStack();
            result = src.copy();
            int boatEnd = rows * 9;
            if (slotIndex < boatEnd) {
                if (!this.insertItem(src, boatEnd, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.insertItem(src, 0, boatEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (src.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    public int getRows() { return rows; }
    public Inventory getInventory() { return inventory; }
}
