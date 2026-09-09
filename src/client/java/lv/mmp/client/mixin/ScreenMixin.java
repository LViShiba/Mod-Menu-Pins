package lv.mmp.client.mixin;

import lv.mmp.client.PinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(
            method = "clickUrlAction(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;Ljava/net/URI;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mmp$autoTrustUrlAction(Minecraft minecraft, Screen screen, URI uri, CallbackInfoReturnable<Boolean> cir) {
        if (PinManager.INSTANCE.isAutoTrustLinks()
                && screen != null
                && screen.getClass().getPackageName().startsWith("com.terraformersmc.modmenu")) {
            Util.getPlatform().openUri(uri);
            cir.setReturnValue(true);
        }
    }
}
