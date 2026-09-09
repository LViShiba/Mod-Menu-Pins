package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import lv.mmp.client.NoFocusRegistry;
import lv.mmp.client.PinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(ModListWidget.class)
public abstract class ModListWidgetMixin {

    @Redirect(
            method = "filter(Ljava/lang/String;ZZ)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V", ordinal = 0)
    )
    private void mmp$sortPinnedFirst(List<Mod> mods, Comparator<? super Mod> original) {
        Comparator<Mod> pinnedFirst = Comparator.<Mod>comparingInt(m -> {
            int index = PinManager.INSTANCE.pinIndex(m.getId());
            return index < 0 ? Integer.MAX_VALUE : index;
        }).thenComparing(original);

        mods.sort(pinnedFirst);
    }

    @Inject(method = "filter(Ljava/lang/String;ZZ)V", at = @At("RETURN"))
    private void mmp$restoreSelectionAfterFilter(String searchTerm, boolean refresh, boolean reposition, CallbackInfo ci) {
        ModListWidget self = (ModListWidget) (Object) this;
        if (self.getSelected() != null) {
            return;
        }
        ModListEntry previouslySelected = self.getParent().getSelectedEntry();
        if (previouslySelected == null) {
            return;
        }
        for (ModListEntry entry : self.children()) {
            if (entry.getMod().equals(previouslySelected.getMod())) {
                self.setSelected(entry);
                return;
            }
        }

        if (previouslySelected instanceof ChildEntry childEntry) {
            Mod parentMod = ((ChildEntryAccessor) childEntry).mmp$getParent().getMod();
            for (ModListEntry entry : self.children()) {
                if (entry.getMod().equals(parentMod)) {
                    self.setSelected(entry);
                    self.ensureVisible(entry);
                    return;
                }
            }
        }
    }

    @Inject(method = "select", at = @At("HEAD"))
    private void mmp$allowFocusOnGenuineSelection(ModListEntry entry, CallbackInfo ci) {
        NoFocusRegistry.unmarkOutlineWhiteSuppressed((ModListWidget) (Object) this);
    }

    @Redirect(method = "extractListItems", at = @At(value = "INVOKE", target = "Lcom/terraformersmc/modmenu/gui/widget/ModListWidget;isFocused()Z"))
    private boolean mmp$suppressOutlineForNonGenuineFocus(ModListWidget instance) {
        return instance.isFocused() && !NoFocusRegistry.isOutlineWhiteSuppressed(instance);
    }
}
