package lv.mmp.client.gui;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum BadgeForm implements NameableEnum {
    DEFAULT("default"),
    HEART("heart"),
    PACIFIER("pacifier");

    private final String id;

    BadgeForm(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static BadgeForm fromId(String id) {
        for (BadgeForm form : values()) {
            if (form.id.equals(id)) {
                return form;
            }
        }
        return DEFAULT;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("form.mod-menu-pins." + this.id);
    }
}
