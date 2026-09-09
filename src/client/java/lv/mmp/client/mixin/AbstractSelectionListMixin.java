package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import lv.mmp.client.NoFocusRegistry;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {

    @Redirect(method = "extractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractSelectionList;isFocused()Z"))
    private boolean mmp$suppressOutlineForNonGenuineFocus(AbstractSelectionList<?> instance) {
        return instance.isFocused() && !NoFocusRegistry.isOutlineWhiteSuppressed((GuiEventListener) instance);
    }
}
