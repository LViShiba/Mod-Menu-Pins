package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import lv.mmp.client.NoFocusRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParentEntry.class)
public abstract class ParentEntryMixin {

    @Shadow
    protected ModListWidget list;

    @Unique
    private double mmp$scrollBeforeToggle;

    @Inject(method = "toggleChildren", at = @At("HEAD"))
    private void mmp$captureScrollBeforeToggle(CallbackInfo ci) {
        mmp$scrollBeforeToggle = this.list.scrollAmount();
        NoFocusRegistry.requestFocusClear();
    }

    @Inject(method = "toggleChildren", at = @At("TAIL"))
    private void mmp$restoreScrollAfterToggle(CallbackInfo ci) {
        this.list.setScrollAmount(mmp$scrollBeforeToggle);
    }
}
