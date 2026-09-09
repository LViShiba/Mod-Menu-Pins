package lv.mmp.client.mixin;

import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import lv.mmp.client.NoFocusRegistry;
import lv.mmp.client.gui.PinsYaclScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ModMenuOptionsScreen.class)
public abstract class ModMenuOptionsScreenMixin {

    @Redirect(method = "addOptions", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
    private void mmp$addOwnConfigButtonToOptionsScreen(OptionsList list, OptionInstance<?>[] options) {
        List<AbstractWidget> widgets = new ArrayList<>(options.length + 1);
        for (OptionInstance<?> option : options) {
            AbstractWidget optionWidget = option.createButton(Minecraft.getInstance().options);
            NoFocusRegistry.markNoFocusRetention(optionWidget);
            widgets.add(optionWidget);
        }
        Button mmpConfigButton = Button.builder(mmp$buildConfigButtonLabel(), button -> {
                    Screen current = (Screen) (Object) this;
                    Minecraft.getInstance().gui.setScreen(PinsYaclScreen.create(current));
                })
                .pos(0, 0)
                .size(150, 20)
                .build();
        NoFocusRegistry.markNoFocusRetention(mmpConfigButton);
        widgets.add(mmpConfigButton);
        list.addSmall(widgets);
    }

    private static MutableComponent mmp$buildConfigButtonLabel() {
        String raw = I18n.get("screen.mod-menu-pins.mmp_config_button");
        int mmpIndex = raw.indexOf("MMP");
        if (mmpIndex < 0) {
            return Component.translatable("screen.mod-menu-pins.mmp_config_button");
        }

        String before = raw.substring(0, mmpIndex);
        String after = raw.substring(mmpIndex + "MMP".length());
        Style firstMStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xBA8E23));
        Style secondMStyle = Style.EMPTY.withColor(TextColor.fromRgb(0x8B0000));
        Style pStyle = Style.EMPTY.withColor(TextColor.fromRgb(0x00008B));

        MutableComponent label = Component.literal(before);
        label.append(Component.literal("M").withStyle(firstMStyle));
        label.append(Component.literal("M").withStyle(secondMStyle));
        label.append(Component.literal("P").withStyle(pStyle));
        label.append(Component.literal(after));
        return label;
    }
}
