package lv.mmp.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import lv.mmp.client.ModEnvironmentUtil;
import lv.mmp.client.PinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ModSearch.class)
public abstract class ModSearchMixin {

    @ModifyReturnValue(method = "search", at = @At("RETURN"))
    private static List<Mod> mmp$applyModMenuFilters(List<Mod> original) {
        if (!PinManager.INSTANCE.isModMenuFilterPinnedOnly()
                && !PinManager.INSTANCE.isModMenuFilterLibraryOnly()
                && "all".equals(PinManager.INSTANCE.getModMenuFilterEnvironment())) {
            return original;
        }
        return original.stream()
                .filter(ModSearchMixin::mmp$passesModMenuFilters)
                .collect(Collectors.toList());
    }

    @Unique
    private static boolean mmp$passesModMenuFilters(Mod mod) {
        if (mmp$passesModMenuFiltersDirect(mod)) {
            return true;
        }
        List<Mod> children = ModMenu.PARENT_MAP.get(mod);
        if (children != null) {
            for (Mod child : children) {
                if (mmp$passesModMenuFilters(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static boolean mmp$passesModMenuFiltersDirect(Mod mod) {
        if (PinManager.INSTANCE.isModMenuFilterPinnedOnly() && !PinManager.INSTANCE.isPinned(mod.getId())) {
            return false;
        }

        if (PinManager.INSTANCE.isModMenuFilterLibraryOnly() && !mod.getBadges().contains(Mod.Badge.LIBRARY)) {
            return false;
        }

        String envFilter = PinManager.INSTANCE.getModMenuFilterEnvironment();
        if (!"all".equals(envFilter) && !envFilter.equals(ModEnvironmentUtil.resolve(mod.getId()))) {
            return false;
        }

        return true;
    }
}
