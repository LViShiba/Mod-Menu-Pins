package lv.mmp.client.mixin;

import lv.mmp.client.NoFocusRegistry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiEventListener.class)
public interface FocusRetentionMixin {

    @Inject(method = "shouldTakeFocusAfterInteraction", at = @At("HEAD"), cancellable = true)
    private void mmp$suppressMarkedWidgets(CallbackInfoReturnable<Boolean> cir) {
        if (NoFocusRegistry.isNoFocusRetention((GuiEventListener) this)) {
            cir.setReturnValue(false);
        }
    }
}
