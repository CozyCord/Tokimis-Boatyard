package net.cozystudios.tokimiboatyard.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpgradedBoatScreen extends HandledScreen<UpgradedBoatScreenHandler> {
    private static final Identifier TEXTURE =
        new Identifier("textures/gui/container/generic_54.png");

    private static final int TOP_BORDER_H = 17;
    private static final int SLOT_ROW_H   = 18;
    private static final int BOTTOM_SECT_H = 96;
    private static final int BOTTOM_SECT_UV_Y = 126;
    private static final int PANEL_WIDTH  = 176;

    private final int rows;

    public UpgradedBoatScreen(UpgradedBoatScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.rows = handler.getRows();
        this.backgroundWidth = PANEL_WIDTH;
        this.backgroundHeight = TOP_BORDER_H + rows * SLOT_ROW_H + BOTTOM_SECT_H;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        ctx.drawTexture(TEXTURE, x, y, 0, 0, PANEL_WIDTH, TOP_BORDER_H);

        for (int r = 0; r < rows; r++) {
            ctx.drawTexture(TEXTURE, x, y + TOP_BORDER_H + r * SLOT_ROW_H,
                0, TOP_BORDER_H, PANEL_WIDTH, SLOT_ROW_H);
        }

        ctx.drawTexture(TEXTURE, x, y + TOP_BORDER_H + rows * SLOT_ROW_H,
            0, BOTTOM_SECT_UV_Y, PANEL_WIDTH, BOTTOM_SECT_H);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(ctx, mouseX, mouseY);
    }
}
