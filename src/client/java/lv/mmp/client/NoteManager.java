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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NoteManager {
    public static final NoteManager INSTANCE = new NoteManager();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mod-menu-pins-notes.json");

    private final Map<String, List<String>> notes = new HashMap<>();
    private boolean loaded = false;

    private NoteManager() {}

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
            if (data != null && data.notes != null) {
                notes.putAll(data.notes);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mod-menu-pins-notes.json", e);
        }
    }

    private void save() {
        Data data = new Data();
        data.notes = notes;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save mod-menu-pins-notes.json", e);
        }
    }

    public List<String> getPages(String modId) {
        ensureLoaded();
        List<String> pages = notes.get(modId);
        if (pages == null || pages.isEmpty()) {
            List<String> fresh = new ArrayList<>();
            fresh.add("");
            return fresh;
        }
        return new ArrayList<>(pages);
    }

    public boolean hasNote(String modId) {
        ensureLoaded();
        List<String> pages = notes.get(modId);
        if (pages == null) {
            return false;
        }
        for (String page : pages) {
            if (!page.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void setPages(String modId, List<String> pages) {
        ensureLoaded();
        boolean allEmpty = pages.isEmpty() || (pages.size() == 1 && pages.get(0).isEmpty());
        if (allEmpty) {
            notes.remove(modId);
        } else {
            notes.put(modId, new ArrayList<>(pages));
        }
        save();
    }

    private static final class Data {
        Map<String, List<String>> notes;
    }
}
