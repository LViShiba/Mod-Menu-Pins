package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import lv.mmp.client.NoFocusRegistry;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ModsScreenClickAwayMixin {

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mmp$clearFocusOnDeadSpaceClick(
            MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValueZ()) {
            return;
        }
        if (!(((Object) this) instanceof ModsScreen screen)) {
            return;
        }

        screen.clearFocus();
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mmp$clearFocusAfterMarkedButtonClick(
            MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (!(((Object) this) instanceof ModsScreen screen)) {
            return;
        }

        GuiEventListener clicked = ((ContainerEventHandler) (Object) this)
                .getChildAt(event.x(), event.y())
                .orElse(null);
        if (clicked != null && NoFocusRegistry.isNoFocusRetention(clicked)) {
            screen.clearFocus();
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mmp$clearFocusAfterFlaggedInteraction(
            MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(((Object) this) instanceof ModsScreen screen)) {
            return;
        }
        if (NoFocusRegistry.consumePendingFocusClear()) {
            screen.clearFocus();
        }
    }
}
