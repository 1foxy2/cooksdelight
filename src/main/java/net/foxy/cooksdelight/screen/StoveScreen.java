package net.foxy.cooksdelight.screen;

import net.foxy.cooksdelight.CooksDelightMod;
import net.foxy.cooksdelight.menu.StoveMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.awt.*;

public class StoveScreen extends AbstractContainerScreen<StoveMenu> implements RecipeUpdateListener {
    private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final ResourceLocation TEXTURE = CooksDelightMod.id("textures/gui/container/stove.png");
    public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;
    private final ResourceLocation texture;
    private final ResourceLocation burnProgressSprite;
    private static final Rectangle HEAT_ICON = new Rectangle(47, 55, 17, 15);

    public StoveScreen(
            StoveMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        this.recipeBookComponent = new SmeltingRecipeBookComponent();
        this.texture = TEXTURE;
        this.burnProgressSprite = BURN_PROGRESS_SPRITE;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, p_313431_ -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            p_313431_.setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX      the x-coordinate of the mouse cursor.
     * @param mouseY      the y-coordinate of the mouse cursor.
     * @param partialTick the partial tick time.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, partialTick);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(this.texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 126, this.topPos + 58, 176, 0, 17, 15);
        }

        int i1 = 24;
        int j1 = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
        guiGraphics.blitSprite(this.burnProgressSprite, 24, 16, 0, 0, x + 89, y + 34, j1, 16);
    }

    /**
     * Called when a mouse button is clicked within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param mouseX the X coordinate of the mouse.
     * @param mouseY the Y coordinate of the mouse.
     * @param button the button that was clicked.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBookComponent.slotClicked(slot);
    }

    /**
     * Called when a keyboard key is pressed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param keyCode   the key code of the pressed key.
     * @param scanCode  the scan code of the pressed key.
     * @param modifiers the keyboard modifiers.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.recipeBookComponent.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean flag = mouseX < (double)guiLeft
                || mouseY < (double)guiTop
                || mouseX >= (double)(guiLeft + this.imageWidth)
                || mouseY >= (double)(guiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
    }

    /**
     * Called when a character is typed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param codePoint the code point of the typed character.
     * @param modifiers the keyboard modifiers.
     */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.recipeBookComponent.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
