package com.everest.transmutated.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/stonecutter.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe");

    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int ENTRY_W = 16;
    private static final int ENTRY_H = 18;

    private int hoveredRecipe = -1;

    private float scrollAmount;
    private boolean scrolling;
    private int scrollOffset;
    private boolean hasInput;

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        handler.setContentsChangedListener(this::onInventoryChange);
        --this.titleY;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int scrollbarY = (int)(41 * scrollAmount);
        ctx.drawGuiTexture(shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE,
                x + 119, y + 15 + scrollbarY, 12, 15);

        int startX = x + 52;
        int startY = y + 14;
        int max = scrollOffset + COLUMNS * ROWS;

        renderRecipeBackground(ctx, mouseX, mouseY, startX, startY, max);
        renderRecipeIcons(ctx, startX, startY, max);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(ctx, mouseX, mouseY);

        if (hoveredRecipe >= 0 && hoveredRecipe < handler.getAvailableRecipeCount()) {
            ItemStack stack = handler.getDisplayResult(hoveredRecipe);
            if (!stack.isEmpty()) {
                ctx.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
            }
        }
    }


    private void renderRecipeBackground(DrawContext ctx, int mouseX, int mouseY, int x, int y, int max) {
        hoveredRecipe = -1;

        for (int i = scrollOffset; i < max && i < handler.getAvailableRecipeCount(); i++) {
            int j = i - scrollOffset;
            int rx = x + (j % COLUMNS) * ENTRY_W;
            int ry = y + (j / COLUMNS) * ENTRY_H + 2;

            boolean hovered =
                    mouseX >= rx && mouseY >= ry &&
                            mouseX < rx + ENTRY_W && mouseY < ry + ENTRY_H;

            if (hovered) {
                hoveredRecipe = i;
            }

            Identifier tex =
                    i == handler.getSelectedRecipe() ? RECIPE_SELECTED_TEXTURE :
                            hovered ? RECIPE_HIGHLIGHTED_TEXTURE
                                    : RECIPE_TEXTURE;

            ctx.drawGuiTexture(tex, rx, ry - 1, ENTRY_W, ENTRY_H);
        }
    }


    private void renderRecipeIcons(DrawContext ctx, int x, int y, int max) {
        for (int i = scrollOffset; i < max && i < handler.getAvailableRecipeCount(); i++) {
            int j = i - scrollOffset;
            int rx = x + (j % COLUMNS) * ENTRY_W;
            int ry = y + (j / COLUMNS) * ENTRY_H + 2;

            ItemStack stack = handler.getDisplayResult(i);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, rx, ry);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hasInput) {
            int x = this.x + 52;
            int y = this.y + 14;
            int max = scrollOffset + COLUMNS * ROWS;

            for (int i = scrollOffset; i < max && i < handler.getAvailableRecipeCount(); i++) {
                int j = i - scrollOffset;
                double dx = mouseX - (x + (j % COLUMNS) * ENTRY_W);
                double dy = mouseY - (y + (j / COLUMNS) * ENTRY_H);

                if (dx >= 0 && dy >= 0 && dx < ENTRY_W && dy < ENTRY_H) {
                    this.client.interactionManager.clickButton(
                            this.handler.syncId,
                            i
                    );
                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(
                                    SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F
                            )
                    );
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    private boolean shouldScroll() {
        return hasInput && handler.getAvailableRecipeCount() > COLUMNS * ROWS;
    }


    private void onInventoryChange() {
        hasInput = handler.hasInput();
        if (!hasInput) {
            scrollAmount = 0;
            scrollOffset = 0;
        }
    }
}