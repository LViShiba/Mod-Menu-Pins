package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChildEntry.class)
public interface ChildEntryAccessor {

    @Accessor("parent")
    ParentEntry mmp$getParent();
}
