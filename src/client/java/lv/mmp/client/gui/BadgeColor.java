package lv.mmp.client.gui;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public enum BadgeColor implements NameableEnum {
    WHITE("white", 0xFFFFFF),
    PINK("pink", 0xFF69B4),
    CRIMSON("crimson", 0xDC143C),
    RED("red", 0xFF5555),
    ORANGE("orange", 0xFFAA00),
    YELLOW("yellow", 0xFFFF55),
    GREEN("green", 0x55FF55),
    CYAN("cyan", 0x55FFFF),
    BLUE("blue", 0x5555FF),
    PURPLE("purple", 0xFF55FF),
    BLACK("black", 0x000000);

    private final String id;
    private final int hex;

    BadgeColor(String id, int hex) {
        this.id = id;
        this.hex = hex;
    }

    public String id() {
        return this.id;
    }

    public static BadgeColor fromId(String id) {
        for (BadgeColor color : values()) {
            if (color.id.equals(id)) {
                return color;
            }
        }
        return RED;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("color.mod-menu-pins." + this.id)
                .copy()
                .withStyle(style -> style.withColor(TextColor.fromRgb(this.hex)));
    }
}
