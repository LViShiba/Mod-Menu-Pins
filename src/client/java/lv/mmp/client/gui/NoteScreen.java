package lv.mmp.client.gui;

import com.google.common.collect.Lists;
import lv.mmp.ModMenuPins;
import lv.mmp.client.NoteManager;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ListIterator;

public class NoteScreen extends Screen {

    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final int MAX_PAGES = 100;

    private static final WidgetSprites NEW_PAGE_SPRITES = new WidgetSprites(
            ModMenuPins.id("note/new_page/unfocused"), ModMenuPins.id("note/new_page/focused"));
    private static final WidgetSprites DELETE_PAGE_SPRITES = new WidgetSprites(
            ModMenuPins.id("note/delete_page/unfocused"), ModMenuPins.id("note/delete_page/focused"));

    private final Screen parent;
    private final String modId;
    private final List<String> pages = Lists.newArrayList();
    private int currentPage;
    private MultiLineEditBox page;
    private PageButton forwardButton;
    private PageButton backButton;
    private ImageButton newPageButton;
    private ImageButton delPageButton;
    private Component numberOfPages = CommonComponents.EMPTY;

    private boolean suppressNextSpaceChar;

    public NoteScreen(Screen parent, String modId, String modName) {
        this(parent, modId, modName, false);
    }

    public NoteScreen(Screen parent, String modId, String modName, boolean suppressNextSpaceChar) {
        super(Component.translatable("screen.mod-menu-pins.note_screen.title", modName));
        this.parent = parent;
        this.modId = modId;
        this.suppressNextSpaceChar = suppressNextSpaceChar;
        this.pages.addAll(NoteManager.INSTANCE.getPages(modId));
        if (this.pages.isEmpty()) {
            this.pages.add("");
        }
    }

    private int getNumPages() {
        return this.pages.size();
    }

    private int backgroundLeft() {
        return (this.width - IMAGE_WIDTH) / 2;
    }

    private int backgroundTop() {
        return 2;
    }

    private int menuControlsTop() {
        return this.backgroundTop() + IMAGE_HEIGHT + 2;
    }

    @Override
    protected void init() {
        int left = this.backgroundLeft();
        int top = this.backgroundTop();

        this.backButton = this.addRenderableWidget(
                new PageButton(left + 43, top + 157, false, button -> this.pageBack(), true)
        );
        this.backButton.setTooltip(Tooltip.create(Component.translatable("book.page_button.previous")));
        this.forwardButton = this.addRenderableWidget(
                new PageButton(left + 116, top + 157, true, button -> this.pageForward(), true)
        );
        this.forwardButton.setTooltip(Tooltip.create(Component.translatable("book.page_button.next")));

        this.newPageButton = this.addRenderableWidget(
                new NoFocusStealImageButton(left + 119, top + 153, 20, 20, NEW_PAGE_SPRITES, button -> this.addNewPage())
        );
        this.newPageButton.setTooltip(Tooltip.create(
                Component.translatable("screen.mod-menu-pins.note_screen.new_page_tooltip")));

        this.delPageButton = this.addRenderableWidget(
                new NoFocusStealImageButton(left + 95, top + 153, 20, 20, DELETE_PAGE_SPRITES, button -> this.deleteCurrentPage())
        );
        this.delPageButton.setTooltip(Tooltip.create(
                Component.translatable("screen.mod-menu-pins.note_screen.delete_page_tooltip")));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.mmp$saveAndReturn())
                .pos(this.width / 2 - 49, this.menuControlsTop())
                .size(98, 20)
                .build());

        this.page = MultiLineEditBox.builder()
                .setShowDecorations(false)
                .setTextColor(0xFF000000)
                .setCursorColor(0xFF000000)
                .setShowBackground(false)
                .setTextShadow(false)
                .setX((this.width - 114) / 2 - 8)
                .setY(28)
                .build(this.font, 122, 134, CommonComponents.EMPTY);
        this.page.setCharacterLimit(1024);
        this.page.setLineLimit(126 / this.font.lineHeight);
        this.page.setValueListener(value -> this.pages.set(this.currentPage, value));
        this.addRenderableWidget(this.page);
        this.updatePageContent();
        this.numberOfPages = this.getPageNumberMessage();

        this.updateButtonVisibility();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.page);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.getPageNumberMessage());
    }

    private Component getPageNumberMessage() {
        return Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages())
                .withColor(0xFF000000)
                .withoutShadow();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.updatePageContent();
        }
        this.updateButtonVisibility();
    }

    private void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            this.currentPage++;
            this.updatePageContent();
        }
        this.updateButtonVisibility();
    }

    private void updatePageContent() {
        this.page.setValue(this.pages.get(this.currentPage), true);
        this.setFocused(this.page);
        this.numberOfPages = this.getPageNumberMessage();
    }

    private void updateButtonVisibility() {
        boolean onFinalPage = this.currentPage == this.getNumPages() - 1;

        this.backButton.visible = this.backButton.active = this.currentPage > 0;
        this.forwardButton.visible = this.forwardButton.active = !onFinalPage;
        this.newPageButton.visible = this.newPageButton.active = onFinalPage;
        this.delPageButton.visible = this.delPageButton.active = this.getNumPages() > 1;
    }

    private void addNewPage() {
        if (this.getNumPages() >= MAX_PAGES) {
            return;
        }
        this.pages.add("");
        this.currentPage = this.getNumPages() - 1;
        this.updatePageContent();
        this.updateButtonVisibility();
    }

    private void deleteCurrentPage() {
        if (this.getNumPages() <= 1) {
            return;
        }
        this.pages.remove(this.currentPage);
        if (this.currentPage >= this.getNumPages()) {
            this.currentPage = this.getNumPages() - 1;
        }
        this.updatePageContent();
        this.updateButtonVisibility();
    }

    private static class NoFocusStealImageButton extends ImageButton {
        private NoFocusStealImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
            super(x, y, width, height, sprites, onPress);
        }

        @Override
        public boolean shouldTakeFocusAfterInteraction() {
            return false;
        }
    }

    private void eraseEmptyTrailingPages() {
        ListIterator<String> it = this.pages.listIterator(this.pages.size());
        while (it.hasPrevious() && it.previous().isEmpty()) {
            it.remove();
        }
    }

    private void mmp$saveAndReturn() {
        this.eraseEmptyTrailingPages();
        NoteManager.INSTANCE.setPages(this.modId, this.pages);
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.mmp$saveAndReturn();
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case 266: {
                this.backButton.onPress(event);
                return true;
            }
            case 267: {
                this.forwardButton.onPress(event);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.suppressNextSpaceChar) {
            this.suppressNextSpaceChar = false;
            if (event.codepoint() == ' ') {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        this.visitText(graphics.textRenderer());
    }

    private void visitText(ActiveTextCollector collector) {
        int left = this.backgroundLeft();
        int top = this.backgroundTop();
        collector.accept(TextAlignment.RIGHT, left + 148, top + 16, this.numberOfPages);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BookViewScreen.BOOK_LOCATION,
                this.backgroundLeft(),
                this.backgroundTop(),
                0.0F,
                0.0F,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                256,
                256
        );
    }
}
