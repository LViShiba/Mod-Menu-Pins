package lv.mmp.client.gui;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import lv.mmp.client.PinManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public final class PinsYaclScreen {

    private PinsYaclScreen() {}

    private static Identifier badgeIconId(String form, String color) {
        String fileName = switch (form) {
            case "heart" -> "icon_pin_heart_" + color + ".png";
            case "pacifier" -> "icon_pin_pacifier.png";
            default -> "icon_pin_" + color + ".png";
        };
        return Identifier.fromNamespaceAndPath("mod-menu-pins", "textures/gui/" + fileName);
    }

    private static int badgeIconWidth(String form) {
        return switch (form) {
            case "heart" -> 8;
            case "pacifier" -> 2;
            default -> 10;
        };
    }

    private static int badgeIconHeight(String form) {
        return badgeIconWidth(form);
    }

    private static final class Holder<T> {
        private T value;
    }

    private static final class BadgePreviewRenderer implements ImageRenderer {
        private final Supplier<String> formIdSupplier;
        private final Supplier<String> colorIdSupplier;

        private BadgePreviewRenderer(Supplier<String> formIdSupplier, Supplier<String> colorIdSupplier) {
            this.formIdSupplier = formIdSupplier;
            this.colorIdSupplier = colorIdSupplier;
        }

        @Override
        public int render(GuiGraphicsExtractor graphics, int x, int y, int renderWidth, float tickDelta) {
            String form = formIdSupplier.get();
            String color = colorIdSupplier.get();
            Identifier location = badgeIconId(form, color);
            int width = badgeIconWidth(form);
            int height = badgeIconHeight(form);

            float ratio = renderWidth / (float) width;
            int targetHeight = (int) (height * ratio);

            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(ratio, ratio);
            graphics.blit(RenderPipelines.GUI_TEXTURED, location, 0, 0, 0.0F, 0.0F, width, height, width, height);
            graphics.pose().popMatrix();

            return targetHeight;
        }

        @Override
        public void close() {}
    }

    public static Screen create(Screen parent) {
        Holder<Option<BadgeForm>> formOptionHolder = new Holder<>();
        Holder<Option<BadgeColor>> colorOptionHolder = new Holder<>();

        Supplier<String> livePendingFormId = () -> formOptionHolder.value.pendingValue().id();
        Supplier<String> livePendingColorId = () -> colorOptionHolder.value.pendingValue().id();

        Option<BadgeForm> formOption = Option.<BadgeForm>createBuilder()
                .name(Component.translatable("screen.mod-menu-pins.badge_form"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("screen.mod-menu-pins.badge_form.tooltip"))
                        .customImage(new BadgePreviewRenderer(livePendingFormId, livePendingColorId))
                        .build())
                .binding(
                        BadgeForm.DEFAULT,
                        () -> BadgeForm.fromId(PinManager.INSTANCE.getBadgeForm()),
                        form -> PinManager.INSTANCE.setBadgeForm(form.id())
                )
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(BadgeForm.class))
                .build();
        formOptionHolder.value = formOption;

        Option<BadgeColor> colorOption = Option.<BadgeColor>createBuilder()
                .name(Component.translatable("screen.mod-menu-pins.badge_color"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("screen.mod-menu-pins.badge_color.tooltip"))
                        .customImage(new BadgePreviewRenderer(livePendingFormId, livePendingColorId))
                        .build())
                .binding(
                        BadgeColor.RED,
                        () -> BadgeColor.fromId(PinManager.INSTANCE.getBadgeColor()),
                        color -> PinManager.INSTANCE.setBadgeColor(color.id())
                )
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(BadgeColor.class))
                .build();
        colorOptionHolder.value = colorOption;

        Option<Boolean> autoTrustOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("screen.mod-menu-pins.auto_trust_links_option"))
                .description(OptionDescription.of(Component.translatable("screen.mod-menu-pins.auto_trust_links.tooltip")))
                .binding(false, PinManager.INSTANCE::isAutoTrustLinks, PinManager.INSTANCE::setAutoTrustLinks)
                .controller(TickBoxControllerBuilder::create)
                .build();

        OptionGroup mainGroup = OptionGroup.createBuilder()
                .name(Component.translatable("screen.mod-menu-pins.group.main"))
                .option(formOption)
                .option(colorOption)
                .option(autoTrustOption)
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("screen.mod-menu-pins.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("screen.mod-menu-pins.category.general"))
                        .group(mainGroup)
                        .build())
                .build()
                .generateScreen(parent);
    }
}
