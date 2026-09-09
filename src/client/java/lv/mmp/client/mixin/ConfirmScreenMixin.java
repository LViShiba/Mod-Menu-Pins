package lv.mmp.client.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lv.mmp.client.PinManager;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin {

    @Shadow
    @Final
    protected BooleanConsumer callback;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void mmp$autoTrustConfirmLink(CallbackInfo ci) {
        if (((Object) this) instanceof ConfirmLinkScreen
                && PinManager.INSTANCE.isAutoTrustLinks()
                && PinManager.INSTANCE.isLastConfirmLinkFromModMenu()) {
            this.callback.accept(true);
            ci.cancel();
        }
    }
}
