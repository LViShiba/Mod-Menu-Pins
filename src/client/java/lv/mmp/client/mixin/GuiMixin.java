package lv.mmp.client.mixin;

import lv.mmp.client.PinManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void mmp$captureConfirmLinkOrigin(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof ConfirmLinkScreen)) {
            return;
        }

        Screen current = ((Gui) (Object) this).screen();
        boolean fromModMenu = current != null
                && current.getClass().getPackageName().startsWith("com.terraformersmc.modmenu");
        PinManager.INSTANCE.setLastConfirmLinkFromModMenu(fromModMenu);
    }
}
