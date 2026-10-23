package net.cozystudios.tokimiboatyard.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.cozystudios.tokimiboatyard.util.ModIds;

public class UpgradedBoatScreen extends AbstractContainerScreen<UpgradedBoatScreenHandler> {
    private static final ResourceLocation TEXTURE =
        ModIds.parse("textures/gui/container/generic_54.png");

    private static final int TOP_BORDER_H = 17;
    private static final int SLOT_ROW_H   = 18;
    private static final int BOTTOM_SECT_H = 96;
    private static final int BOTTOM_SECT_UV_Y = 126;
    private static final int PANEL_WIDTH  = 176;

    private final int rows;

    public UpgradedBoatScreen(UpgradedBoatScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
        this.rows = handler.getRows();
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = TOP_BORDER_H + rows * SLOT_ROW_H + BOTTOM_SECT_H;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        ctx.blit(TEXTURE, x, y, 0, 0, PANEL_WIDTH, TOP_BORDER_H);

        for (int r = 0; r < rows; r++) {
            ctx.blit(TEXTURE, x, y + TOP_BORDER_H + r * SLOT_ROW_H,
                0, TOP_BORDER_H, PANEL_WIDTH, SLOT_ROW_H);
        }

        ctx.blit(TEXTURE, x, y + TOP_BORDER_H + rows * SLOT_ROW_H,
            0, BOTTOM_SECT_UV_Y, PANEL_WIDTH, BOTTOM_SECT_H);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        //? if <1.21.1 {
        this.renderBackground(ctx);
        //? }
        super.render(ctx, mouseX, mouseY, delta);
        this.renderTooltip(ctx, mouseX, mouseY);
    }
}
