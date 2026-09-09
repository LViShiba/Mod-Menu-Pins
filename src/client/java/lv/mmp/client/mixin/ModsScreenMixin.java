package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import lv.mmp.ModMenuPins;
import lv.mmp.client.NoFocusRegistry;
import lv.mmp.client.PinManager;
import lv.mmp.client.gui.NoteScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(ModsScreen.class)
public abstract class ModsScreenMixin {

    @Shadow private AbstractWidget sortingButton;
    @Shadow private AbstractWidget librariesButton;
    @Shadow private SpriteIconButton filtersButton;
    @Shadow private SpriteIconButton configureButton;
    @Shadow private ModListWidget modList;
    @Shadow private boolean filterOptionsShown;

    @Unique
    private Button mmp$combinedModeButton;

    @Unique
    private static final Identifier NOTE_BUTTON_SPRITE = ModMenuPins.id("note/icon");

    @Unique
    private static int mmp$measureFixedButtonWidth(Font font) {
        Component defaultSortingLabel = CommonComponents.optionNameValue(
                Component.translatable("option.modmenu.sorting"),
                Component.translatable(
                        "option.modmenu.sorting." + ModMenuConfig.SORTING.getDefaultValue().name().toLowerCase(Locale.ROOT)
                )
        );
        return font.width(defaultSortingLabel) + 28;
    }

    @Unique
    private ScreenAccessor mmp$screen() {
        return (ScreenAccessor) (Object) this;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mmp$replaceSortButtonWithCombinedCycle(CallbackInfo ci) {
        int rowY = this.sortingButton.getY();
        int width = mmp$measureFixedButtonWidth(mmp$screen().mmp$getFont());
        int rowX = this.librariesButton.getX() - width - 2;

        this.mmp$combinedModeButton = Button.builder(mmp$combinedModeLabel(mmp$currentCombinedIndex()), button -> {
                    int previous = mmp$currentCombinedIndex();
                    int next = previous + (mmp$screen().mmp$getMinecraft().hasShiftDown() ? -1 : 1);
                    next = ((next % 7) + 7) % 7;
                    mmp$applyCombinedIndex(next);
                    if (previous == 5 && next != 5) {
                        ModMenuConfig.SHOW_LIBRARIES.setValue(ModMenuConfig.LibraryVisibility.WITH_CONFIG);
                        ModMenuConfigManager.save();
                    }
                    button.setMessage(mmp$combinedModeLabel(next));
                    this.librariesButton.setMessage(ModMenuConfig.SHOW_LIBRARIES.getButtonText());
                    this.modList.reloadFilters();
                    this.modList.setScrollAmount(0);
                    if (!this.modList.children().isEmpty()) {
                        this.modList.setSelected(this.modList.getEntry(0));
                    }
                })
                .pos(rowX, rowY)
                .size(width, this.sortingButton.getHeight())
                .build();
        this.mmp$combinedModeButton.visible = this.filterOptionsShown;
        mmp$screen().mmp$addRenderableWidget(this.mmp$combinedModeButton);

        this.sortingButton.visible = false;

        NoFocusRegistry.markNoFocusRetention(this.filtersButton);
        NoFocusRegistry.markNoFocusRetention(this.librariesButton);
        NoFocusRegistry.markNoFocusRetention(this.mmp$combinedModeButton);
    }

    @Inject(method = "setFilterOptionsShown", at = @At("TAIL"))
    private void mmp$syncCombinedModeButtonVisibility(boolean filterOptionsShown, CallbackInfo ci) {
        this.sortingButton.visible = false;
        if (this.mmp$combinedModeButton != null) {
            this.mmp$combinedModeButton.visible = filterOptionsShown;
        }
    }

    @Unique
    private static int mmp$currentCombinedIndex() {
        if (PinManager.INSTANCE.isModMenuFilterPinnedOnly()) {
            return 3;
        }
        if (PinManager.INSTANCE.isModMenuFilterLibraryOnly()) {
            return 5;
        }
        String env = PinManager.INSTANCE.getModMenuFilterEnvironment();
        return switch (env) {
            case "client" -> 4;
            case "universal" -> 6;
            default -> switch (ModMenuConfig.SORTING.getValue()) {
                case ASCENDING -> 0;
                case DESCENDING -> 1;
                case HAS_UPDATE -> 2;
            };
        };
    }

    @Unique
    private static void mmp$applyCombinedIndex(int index) {
        switch (index) {
            case 0 -> {
                ModMenuConfig.SORTING.setValue(ModMenuConfig.Sorting.ASCENDING);
                ModMenuConfigManager.save();
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("all");
            }
            case 1 -> {
                ModMenuConfig.SORTING.setValue(ModMenuConfig.Sorting.DESCENDING);
                ModMenuConfigManager.save();
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("all");
            }
            case 2 -> {
                ModMenuConfig.SORTING.setValue(ModMenuConfig.Sorting.HAS_UPDATE);
                ModMenuConfigManager.save();
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("all");
            }
            case 3 -> {
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(true);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("all");
            }
            case 4 -> {
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("client");
            }
            case 5 -> {
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(true);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("all");
                ModMenuConfig.SHOW_LIBRARIES.setValue(ModMenuConfig.LibraryVisibility.TRUE);
                ModMenuConfigManager.save();
            }
            case 6 -> {
                PinManager.INSTANCE.setModMenuFilterPinnedOnly(false);
                PinManager.INSTANCE.setModMenuFilterLibraryOnly(false);
                PinManager.INSTANCE.cycleModMenuFilterEnvironmentTo("universal");
            }
            default -> throw new IllegalArgumentException("Invalid combined mode index: " + index);
        }
    }

    @Unique
    private static Component mmp$combinedModeLabel(int index) {
        return switch (index) {
            case 0, 1, 2 -> ModMenuConfig.SORTING.getButtonText();
            case 3 -> mmp$pinnedOnlyLabel();
            case 5 -> mmp$libraryOnlyLabel();
            default -> mmp$environmentLabel();
        };
    }

    @Unique
    private static Component mmp$pinnedOnlyLabel() {
        return Component.translatable("screen.mod-menu-pins.filter.combined.pinned_only");
    }

    @Unique
    private static Component mmp$libraryOnlyLabel() {
        return Component.translatable("screen.mod-menu-pins.filter.combined.library_only");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mmp$addNoteButton(CallbackInfo ci) {
        ModsScreen self = (ModsScreen) (Object) this;

        int x;
        int y;
        if (this.configureButton != null) {
            x = this.configureButton.getX() - 20 - 2;
            y = this.configureButton.getY();
        } else {
            x = mmp$screen().mmp$getWidth() - 24;
            y = 48;
        }

        SpriteIconButton noteButton = SpriteIconButton.builder(
                        Component.translatable("screen.mod-menu-pins.note_button_tooltip"),
                        button -> {
                            ModListEntry entry = self.getSelectedEntry();
                            if (entry == null) {
                                return;
                            }
                            Mod mod = entry.getMod();
                            mmp$screen().mmp$getMinecraft().gui.setScreen(
                                    new NoteScreen(self, mod.getId(), mod.getTranslatedName())
                            );
                        },
                        true
                )
                .size(20, 20)
                .tooltip(Component.translatable("screen.mod-menu-pins.note_button_tooltip"))
                .sprite(NOTE_BUTTON_SPRITE, 14, 14)
                .build();
        noteButton.setPosition(x, y);

        NoFocusRegistry.markNoFocusRetention(noteButton);
        mmp$screen().mmp$addRenderableWidget(noteButton);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mmp$openNoteOnSpace(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() != GLFW.GLFW_KEY_SPACE) {
            return;
        }

        ModsScreen self = (ModsScreen) (Object) this;
        if (self.getFocused() != this.modList) {
            return;
        }

        ModListEntry entry = self.getSelectedEntry();
        if (entry == null) {
            return;
        }

        Mod mod = entry.getMod();
        mmp$screen().mmp$getMinecraft().gui.setScreen(
                new NoteScreen(self, mod.getId(), mod.getTranslatedName(), true));
        cir.setReturnValue(true);
    }

    @Unique
    private static Component mmp$environmentLabel() {
        String state = PinManager.INSTANCE.getModMenuFilterEnvironment();
        return Component.translatable("screen.mod-menu-pins.filter.combined." + state + "_only");
    }
}
