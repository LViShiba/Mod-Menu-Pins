package lv.mmp.client;

import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Map;
import java.util.WeakHashMap;

public final class NoFocusRegistry {

    private static final Map<GuiEventListener, Boolean> MARKED = new WeakHashMap<>();

    private static final Map<GuiEventListener, Boolean> OUTLINE_WHITE_SUPPRESSED = new WeakHashMap<>();

    private static boolean mmp$pendingFocusClear = false;

    private NoFocusRegistry() {}

    public static void markNoFocusRetention(GuiEventListener widget) {
        if (widget != null) {
            MARKED.put(widget, Boolean.TRUE);
        }
    }

    public static boolean isNoFocusRetention(GuiEventListener widget) {
        return MARKED.getOrDefault(widget, Boolean.FALSE);
    }

    public static void markOutlineWhiteSuppressed(GuiEventListener widget) {
        if (widget != null) {
            OUTLINE_WHITE_SUPPRESSED.put(widget, Boolean.TRUE);
        }
    }

    public static void unmarkOutlineWhiteSuppressed(GuiEventListener widget) {
        if (widget != null) {
            OUTLINE_WHITE_SUPPRESSED.remove(widget);
        }
    }

    public static boolean isOutlineWhiteSuppressed(GuiEventListener widget) {
        return OUTLINE_WHITE_SUPPRESSED.getOrDefault(widget, Boolean.FALSE);
    }

    public static void requestFocusClear() {
        mmp$pendingFocusClear = true;
    }

    public static boolean consumePendingFocusClear() {
        boolean pending = mmp$pendingFocusClear;
        mmp$pendingFocusClear = false;
        return pending;
    }
}
