package lv.mmp.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import lv.mmp.client.PinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ModListEntry.class)
public abstract class ModListEntryMixin {

    @Shadow public Mod mod;
    @Shadow protected ModListWidget list;

    @Shadow
    public abstract void updatePlacement(int leftX, int width, int y);

    @Shadow
    public abstract int getXOffset();

    @Shadow
    public abstract void openConfig();

    @Shadow
    protected long sinceLastClick;

    @Unique
    private static final int mmp$FULL_ICON_SIZE = 32;
    @Unique
    private static final int mmp$COMPACT_ICON_SIZE = 19;

    @Unique
    private static final int mmp$PIN_ICON_SIZE = 10;
    @Unique
    private static final long mmp$DOUBLE_CLICK_MS = 250L;
    @Unique
    private static final double mmp$DRAG_SWAP_THRESHOLD = 10.0;
    @Unique
    private static final long mmp$ANIM_DURATION_MS = 150L;

    @Unique
    private long mmp$lastRightClickTime = -mmp$DOUBLE_CLICK_MS * 10;
    @Unique
    private double mmp$dragAccum = 0.0;

    @Unique
    private static final Map<String, Integer> mmp$trueY = new HashMap<>();
    @Unique
    private static final Map<String, Integer> mmp$animFromY = new HashMap<>();
    @Unique
    private static final Map<String, Long> mmp$animStartMs = new HashMap<>();
    @Unique
    private static boolean mmp$reorderInProgress = false;
    @Unique
    private static boolean mmp$suppressPlacementHook = false;

    @Unique
    private static Identifier mmp$pinIconId() {
        String color = PinManager.INSTANCE.getBadgeColor();
        String form = PinManager.INSTANCE.getBadgeForm();
        String fileName = switch (form) {
            case "heart" -> "icon_pin_heart_" + color + ".png";
            case "pacifier" -> "icon_pin_pacifier.png";
            default -> "icon_pin_" + color + ".png";
        };
        return Identifier.fromNamespaceAndPath("mod-menu-pins", "textures/gui/" + fileName);
    }

    @Unique
    private void mmp$reorderList() {
        double prevScroll = this.list.scrollAmount();
        mmp$reorderInProgress = true;
        this.list.filter(this.list.getParent().getSearchInput(), true, true);
        mmp$reorderInProgress = false;
        this.list.setScrollAmount(prevScroll);
    }

    @ModifyVariable(method = "extractContent", at = @At("STORE"), ordinal = 1)
    private boolean mmp$fixHoveringIconLeftEdge(
            boolean hoveringIcon,
            GuiGraphicsExtractor drawContext, int mouseX, int mouseY, boolean hovered, float delta,
            @Local(ordinal = 2) int x
    ) {
        if (this.getXOffset() == 0) {
            return hoveringIcon;
        }
        return hoveringIcon && mouseX >= x;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mmp$blockDeadZoneClicks(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        int offset = this.getXOffset();
        if (offset == 0) {
            return;
        }

        double relativeX = click.x() - (this.list.getRowLeft() + offset);
        if (relativeX < 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mmp$onRightDoubleClick(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        boolean rightDown = GLFW.glfwGetMouseButton(client.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (!rightDown) { return; }

        long now = Util.getMillis();
        boolean isSecondClick = now - mmp$lastRightClickTime < mmp$DOUBLE_CLICK_MS;

        if (isSecondClick) {
            PinManager.INSTANCE.togglePin(this.mod.getId());
            mmp$reorderList();
            mmp$lastRightClickTime = -mmp$DOUBLE_CLICK_MS * 10;
            cir.setReturnValue(true);
        } else {
            mmp$lastRightClickTime = now;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mmp$fixChildConfigButtonHitbox(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        int offset = this.getXOffset();
        if (offset == 0) {
            return;
        }

        double relativeX = click.x() - (this.list.getRowLeft() + offset);
        if (relativeX < 0) {
            return;
        }

        ModListEntry self = (ModListEntry) (Object) this;
        this.list.select(self);

        if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen(this.mod.getId())) {
            int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? mmp$COMPACT_ICON_SIZE : mmp$FULL_ICON_SIZE;
            if (relativeX <= iconSize) {
                this.openConfig();
            } else if (Util.getMillis() - this.sinceLastClick < 250) {
                this.openConfig();
            }
        }

        this.sinceLastClick = Util.getMillis();
        cir.setReturnValue(true);
    }

    @Inject(method = "updatePlacement", at = @At("HEAD"))
    private void mmp$onUpdatePlacement(int leftX, int width, int y, CallbackInfo ci) {
        if (mmp$suppressPlacementHook) { return; }
        String id = this.mod.getId();
        Integer prevTrue = mmp$trueY.get(id);
        if (mmp$reorderInProgress && prevTrue != null && prevTrue != y) {
            mmp$animFromY.put(id, prevTrue);
            mmp$animStartMs.put(id, Util.getMillis());
        }
        mmp$trueY.put(id, y);
    }

    @Inject(method = "extractContent", at = @At("HEAD"))
    private void mmp$applyAnimatedPosition(
            GuiGraphicsExtractor drawContext, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci
    ) {
        String id = this.mod.getId();
        Long start = mmp$animStartMs.get(id);
        Integer trueY = mmp$trueY.get(id);
        if (start == null || trueY == null) { return; }

        long elapsed = Util.getMillis() - start;
        if (elapsed >= mmp$ANIM_DURATION_MS) {
            mmp$animStartMs.remove(id);
            mmp$animFromY.remove(id);
            return;
        }

        int fromY = mmp$animFromY.getOrDefault(id, trueY);
        double progress = elapsed / (double) mmp$ANIM_DURATION_MS;
        double eased = 1 - Math.pow(1 - progress, 3);
        int visualY = (int) Math.round(fromY + (trueY - fromY) * eased);

        mmp$suppressPlacementHook = true;
        this.updatePlacement(this.list.getRowLeft(), this.list.getRowWidth(), visualY);
        mmp$suppressPlacementHook = false;
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void mmp$restoreTruePosition(
            GuiGraphicsExtractor drawContext, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci
    ) {
        String id = this.mod.getId();
        if (!mmp$animStartMs.containsKey(id)) { return; }
        Integer trueY = mmp$trueY.get(id);
        if (trueY == null) { return; }
        mmp$suppressPlacementHook = true;
        this.updatePlacement(this.list.getRowLeft(), this.list.getRowWidth(), trueY);
        mmp$suppressPlacementHook = false;
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void mmp$drawPinBadge(
            GuiGraphicsExtractor drawContext, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci,
            @Local(ordinal = 2) int x, @Local(ordinal = 3) int y
    ) {
        if (!PinManager.INSTANCE.isPinned(this.mod.getId())) { return; }
        drawContext.blit(
                RenderPipelines.GUI_TEXTURED, mmp$pinIconId(), x - 2, y - 2, 0.0F, 0.0F,
                mmp$PIN_ICON_SIZE, mmp$PIN_ICON_SIZE, mmp$PIN_ICON_SIZE, mmp$PIN_ICON_SIZE, ARGB.white(1.0F)
        );
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!PinManager.INSTANCE.isPinned(this.mod.getId())) { return false; }

        ModListEntry self = (ModListEntry) (Object) this;
        ModListEntry target = this.list.getEntryAtPos(event.x(), event.y());

        if (target == null || target == self || !PinManager.INSTANCE.isPinned(target.getMod().getId())) {
            mmp$dragAccum = 0.0;
            return true;
        }

        mmp$dragAccum += dy;

        if (Math.abs(mmp$dragAccum) >= mmp$DRAG_SWAP_THRESHOLD) {
            int targetIndex = PinManager.INSTANCE.pinIndex(target.getMod().getId());
            PinManager.INSTANCE.moveTo(this.mod.getId(), targetIndex);
            mmp$reorderList();
            mmp$dragAccum = 0.0;
        }

        return true;
    }
}
