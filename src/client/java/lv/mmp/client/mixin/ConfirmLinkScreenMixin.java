package lv.mmp.client.mixin;

import lv.mmp.client.PinManager;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;

@Mixin(ConfirmLinkScreen.class)
public abstract class ConfirmLinkScreenMixin {

    @Unique
    private static boolean mmp$isModMenuScreen(Screen screen) {
        return screen != null && screen.getClass().getPackageName().startsWith("com.terraformersmc.modmenu");
    }

    @Inject(
            method = "confirmLinkNow(Lnet/minecraft/client/gui/screens/Screen;Ljava/lang/String;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mmp$autoTrustStringWithFlag(Screen screen, String link, boolean showWarning, CallbackInfo ci) {
        if (PinManager.INSTANCE.isAutoTrustLinks() && mmp$isModMenuScreen(screen)) {
            Util.getPlatform().openUri(link);
            ci.cancel();
        }
    }

    @Inject(
            method = "confirmLinkNow(Lnet/minecraft/client/gui/screens/Screen;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mmp$autoTrustString(Screen screen, String link, CallbackInfo ci) {
        if (PinManager.INSTANCE.isAutoTrustLinks() && mmp$isModMenuScreen(screen)) {
            Util.getPlatform().openUri(link);
            ci.cancel();
        }
    }

    @Inject(
            method = "confirmLinkNow(Lnet/minecraft/client/gui/screens/Screen;Ljava/net/URI;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mmp$autoTrustUriWithFlag(Screen screen, URI link, boolean showWarning, CallbackInfo ci) {
        if (PinManager.INSTANCE.isAutoTrustLinks() && mmp$isModMenuScreen(screen)) {
            Util.getPlatform().openUri(link.toString());
            ci.cancel();
        }
    }

    @Inject(
            method = "confirmLinkNow(Lnet/minecraft/client/gui/screens/Screen;Ljava/net/URI;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mmp$autoTrustUri(Screen screen, URI link, CallbackInfo ci) {
        if (PinManager.INSTANCE.isAutoTrustLinks() && mmp$isModMenuScreen(screen)) {
            Util.getPlatform().openUri(link.toString());
            ci.cancel();
        }
    }
}
