package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import lv.mmp.client.NoFocusRegistry;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerWidget.class)
public abstract class AbstractContainerWidgetMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mmp$defaultModListOutlineToGray(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ModListWidget list) {
            NoFocusRegistry.markOutlineWhiteSuppressed(list);
        }
    }
}
