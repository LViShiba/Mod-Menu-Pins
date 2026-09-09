package lv.mmp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PinManager {
    public static final PinManager INSTANCE = new PinManager();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mod-menu-pins.json");

    private final List<String> pinnedModIds = new ArrayList<>();
    private String badgeColor = "red";
    private String badgeForm = "default";
    private boolean autoTrustLinks = false;
    private boolean loaded = false;
    private boolean lastConfirmLinkFromModMenu = false;

    private static final String[] MOD_MENU_ENV_STATES = {"all", "client", "universal"};
    private boolean modMenuFilterPinnedOnly = false;
    private String modMenuFilterEnvironment = "all";
    private boolean modMenuFilterLibraryOnly = false;

    private PinManager() {}

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                if (data.pinned != null) {
                    pinnedModIds.clear();
                    pinnedModIds.addAll(data.pinned);
                }
                if (data.badgeColor != null) {
                    badgeColor = "lvshb".equals(data.badgeColor) ? "red" : data.badgeColor;
                }
                if (data.badgeForm != null) {
                    badgeForm = data.badgeForm;
                }
                autoTrustLinks = data.autoTrustLinks;
                modMenuFilterPinnedOnly = data.modMenuFilterPinnedOnly;
                if (data.modMenuFilterEnvironment != null) {
                    modMenuFilterEnvironment = data.modMenuFilterEnvironment;
                }
                modMenuFilterLibraryOnly = data.modMenuFilterLibraryOnly;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mod-menu-pins.json", e);
        }
    }

    private void save() {
        Data data = new Data();
        data.pinned = pinnedModIds;
        data.badgeColor = badgeColor;
        data.badgeForm = badgeForm;
        data.autoTrustLinks = autoTrustLinks;
        data.modMenuFilterPinnedOnly = modMenuFilterPinnedOnly;
        data.modMenuFilterEnvironment = modMenuFilterEnvironment;
        data.modMenuFilterLibraryOnly = modMenuFilterLibraryOnly;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save mod-menu-pins.json", e);
        }
    }

    public boolean isPinned(String modId) {
        ensureLoaded();
        return pinnedModIds.contains(modId);
    }

    public int pinIndex(String modId) {
        ensureLoaded();
        return pinnedModIds.indexOf(modId);
    }

    public void togglePin(String modId) {
        ensureLoaded();
        if (pinnedModIds.contains(modId)) {
            pinnedModIds.remove(modId);
        } else {
            pinnedModIds.add(modId);
        }
        save();
    }

    public List<String> getPinnedModIds() {
        ensureLoaded();
        return List.copyOf(pinnedModIds);
    }

    public void moveTo(String modId, int newIndex) {
        ensureLoaded();
        if (!pinnedModIds.remove(modId)) {
            return;
        }
        int clamped = Math.max(0, Math.min(newIndex, pinnedModIds.size()));
        pinnedModIds.add(clamped, modId);
        save();
    }

    public String getBadgeColor() {
        ensureLoaded();
        return badgeColor;
    }

    public void setBadgeColor(String color) {
        ensureLoaded();
        this.badgeColor = color;
        save();
    }

    public String getBadgeForm() {
        ensureLoaded();
        return badgeForm;
    }

    public void setBadgeForm(String form) {
        ensureLoaded();
        this.badgeForm = form;
        save();
    }

    public boolean isAutoTrustLinks() {
        ensureLoaded();
        return autoTrustLinks;
    }

    public void setAutoTrustLinks(boolean autoTrustLinks) {
        ensureLoaded();
        this.autoTrustLinks = autoTrustLinks;
        save();
    }

    public boolean isLastConfirmLinkFromModMenu() {
        return lastConfirmLinkFromModMenu;
    }

    public void setLastConfirmLinkFromModMenu(boolean lastConfirmLinkFromModMenu) {
        this.lastConfirmLinkFromModMenu = lastConfirmLinkFromModMenu;
    }

    public boolean isModMenuFilterPinnedOnly() {
        ensureLoaded();
        return modMenuFilterPinnedOnly;
    }

    public void setModMenuFilterPinnedOnly(boolean modMenuFilterPinnedOnly) {
        ensureLoaded();
        this.modMenuFilterPinnedOnly = modMenuFilterPinnedOnly;
        save();
    }

    public String getModMenuFilterEnvironment() {
        ensureLoaded();
        return modMenuFilterEnvironment;
    }

    public void cycleModMenuFilterEnvironment(int direction) {
        ensureLoaded();
        int index = 0;
        for (int i = 0; i < MOD_MENU_ENV_STATES.length; i++) {
            if (MOD_MENU_ENV_STATES[i].equals(modMenuFilterEnvironment)) {
                index = i;
                break;
            }
        }
        int next = (index + direction) % MOD_MENU_ENV_STATES.length;
        if (next < 0) {
            next += MOD_MENU_ENV_STATES.length;
        }
        modMenuFilterEnvironment = MOD_MENU_ENV_STATES[next];
        save();
    }

    public void cycleModMenuFilterEnvironmentTo(String value) {
        ensureLoaded();
        this.modMenuFilterEnvironment = value;
        save();
    }

    public boolean isModMenuFilterLibraryOnly() {
        ensureLoaded();
        return modMenuFilterLibraryOnly;
    }

    public void setModMenuFilterLibraryOnly(boolean modMenuFilterLibraryOnly) {
        ensureLoaded();
        this.modMenuFilterLibraryOnly = modMenuFilterLibraryOnly;
        save();
    }

    private static final class Data {
        List<String> pinned;
        String badgeColor;
        String badgeForm;
        boolean autoTrustLinks;
        boolean modMenuFilterPinnedOnly;
        String modMenuFilterEnvironment;
        boolean modMenuFilterLibraryOnly;
    }
}
