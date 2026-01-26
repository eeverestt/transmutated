package com.everest.transmutated.client.screen;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.recipe.TransmutationRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/stonecutter.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_TEXTURE = Identifier.ofVanilla("container/stonecutter/recipe");
    private static final Identifier PROGRESS_BAR_TEXTURE = Transmutated.id("textures/gui/transmutation_progress.png");

    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int RECIPE_LIST_COLUMNS = 4;
    private static final int RECIPE_LIST_ROWS = 3;
    private static final int RECIPE_ENTRY_WIDTH = 16;
    private static final int RECIPE_ENTRY_HEIGHT = 18;
    private static final int SCROLLBAR_AREA_HEIGHT = 54;
    private static final int RECIPE_LIST_OFFSET_X = 52;
    private static final int RECIPE_LIST_OFFSET_Y = 14;
    private static final int PROGRESS_BAR_U = 0;
    private static final int PROGRESS_BAR_V = 0;
    private static final int PROGRESS_BAR_WIDTH = 24;
    private static final int PROGRESS_BAR_HEIGHT = 17;
    private static final int PROGRESS_BAR_X = 89;
    private static final int PROGRESS_BAR_Y = 34;

    private float scrollAmount;
    private boolean mouseClicked;
    private int scrollOffset;
    private boolean canCraft;

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.setContentsChangedListener(this::onInventoryChange);
        --this.titleY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int scrollbarY = (int)(41.0F * this.scrollAmount);
        Identifier scrollbarSprite = this.shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        context.drawGuiTexture(scrollbarSprite, x + 119, y + 15 + scrollbarY, 12, 15);

        int recipeAreaX = x + RECIPE_LIST_OFFSET_X;
        int recipeAreaY = y + RECIPE_LIST_OFFSET_Y;
        int maxVisible = this.scrollOffset + 12;
        this.renderRecipeBackground(context, mouseX, mouseY, recipeAreaX, recipeAreaY, maxVisible);
        this.renderRecipeIcons(context, recipeAreaX, recipeAreaY, maxVisible);

        int progress = this.handler.getProgress();
        int maxProgress = this.handler.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            context.drawTexture(PROGRESS_BAR_TEXTURE,
                    x + PROGRESS_BAR_X,
                    y + PROGRESS_BAR_Y,
                    0, 0,
                    PROGRESS_BAR_WIDTH,
                    PROGRESS_BAR_HEIGHT);

            int progressWidth = (int)((PROGRESS_BAR_WIDTH - 2) * (float)progress / maxProgress);
            context.drawTexture(PROGRESS_BAR_TEXTURE,
                    x + PROGRESS_BAR_X + 1,
                    y + PROGRESS_BAR_Y + 1,
                    0, PROGRESS_BAR_HEIGHT,
                    progressWidth,
                    PROGRESS_BAR_HEIGHT - 2);
        }
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(context, mouseX, mouseY);

        if (this.canCraft) {
            int areaX = this.x + RECIPE_LIST_OFFSET_X;
            int areaY = this.y + RECIPE_LIST_OFFSET_Y;
            int maxVisible = this.scrollOffset + 12;
            List<RecipeEntry<TransmutationRecipe>> recipes = this.handler.getAvailableRecipes();

            for (int i = this.scrollOffset; i < maxVisible && i < this.handler.getAvailableRecipeCount(); ++i) {
                int slotIndex = i - this.scrollOffset;
                int slotX = areaX + slotIndex % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH;
                int slotY = areaY + slotIndex / RECIPE_LIST_COLUMNS * RECIPE_ENTRY_HEIGHT + 2;

                if (mouseX >= slotX && mouseX < slotX + RECIPE_ENTRY_WIDTH &&
                        mouseY >= slotY && mouseY < slotY + RECIPE_ENTRY_HEIGHT) {

                    RecipeEntry<?> recipe = recipes.get(i);
                    ItemStack result = recipe.value().getResult(this.client.world.getRegistryManager());
                    context.drawItemTooltip(this.textRenderer, result, mouseX, mouseY);
                }
            }
        }
    }

    private void renderRecipeBackground(DrawContext context, int mouseX, int mouseY, int x, int y, int scrollOffset) {
        for (int i = this.scrollOffset; i < scrollOffset && i < this.handler.getAvailableRecipeCount(); ++i) {
            int j = i - this.scrollOffset;
            int k = x + j % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH;
            int l = j / RECIPE_LIST_COLUMNS;
            int m = y + l * RECIPE_ENTRY_HEIGHT + 2;
            Identifier identifier;

            if (i == this.handler.getSelectedRecipe()) {
                identifier = RECIPE_SELECTED_TEXTURE;
            } else if (mouseX >= k && mouseY >= m && mouseX < k + RECIPE_ENTRY_WIDTH && mouseY < m + RECIPE_ENTRY_HEIGHT) {
                identifier = RECIPE_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = RECIPE_TEXTURE;
            }

            context.drawGuiTexture(identifier, k, m - 1, RECIPE_ENTRY_WIDTH, RECIPE_ENTRY_HEIGHT);
        }
    }

    private void renderRecipeIcons(DrawContext context, int x, int y, int scrollOffset) {
        List<RecipeEntry<TransmutationRecipe>> recipes = this.handler.getAvailableRecipes();

        for (int i = this.scrollOffset; i < scrollOffset && i < this.handler.getAvailableRecipeCount(); ++i) {
            int j = i - this.scrollOffset;
            int k = x + j % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH;
            int l = j / RECIPE_LIST_COLUMNS;
            int m = y + l * RECIPE_ENTRY_HEIGHT + 2;

            RecipeEntry<?> recipe = recipes.get(i);
            ItemStack result = recipe.value().getResult(this.client.world.getRegistryManager());
            context.drawItem(result, k, m);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;

        if (this.canCraft) {
            int areaX = this.x + RECIPE_LIST_OFFSET_X;
            int areaY = this.y + RECIPE_LIST_OFFSET_Y;
            int maxVisible = this.scrollOffset + 12;

            for (int i = this.scrollOffset; i < maxVisible; ++i) {
                int j = i - this.scrollOffset;
                double d = mouseX - (double)(areaX + j % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH);
                double e = mouseY - (double)(areaY + j / RECIPE_LIST_COLUMNS * RECIPE_ENTRY_HEIGHT);

                if (d >= 0.0 && e >= 0.0 && d < 16.0 && e < 18.0 &&
                        this.handler.onButtonClick(this.client.player, i)) {

                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.client.interactionManager.clickButton(this.handler.syncId, i);
                    return true;
                }
            }

            int scrollbarX = this.x + 119;
            int scrollbarY = this.y + 9;
            if (mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_WIDTH &&
                    mouseY >= scrollbarY && mouseY < scrollbarY + SCROLLBAR_AREA_HEIGHT) {
                this.mouseClicked = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + 54;
            this.scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5) * 4;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            float f = (float)verticalAmount / (float)i;
            this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)i) + 0.5) * 4;
        }
        return true;
    }

    private boolean shouldScroll() {
        return this.canCraft && this.handler.getAvailableRecipeCount() > 12;
    }

    private int getMaxScroll() {
        return (this.handler.getAvailableRecipeCount() + 4 - 1) / 4 - 3;
    }

    private void onInventoryChange() {
        this.canCraft = this.handler.canCraft();
        if (!this.canCraft) {
            this.scrollAmount = 0.0F;
            this.scrollOffset = 0;
        }
    }
}
