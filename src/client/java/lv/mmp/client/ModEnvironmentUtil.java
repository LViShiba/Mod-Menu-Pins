package lv.mmp.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModEnvironment;

public final class ModEnvironmentUtil {

    private ModEnvironmentUtil() {}

    public static String resolve(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> {
                    ModEnvironment environment = container.getMetadata().getEnvironment();
                    return switch (environment) {
                        case CLIENT -> "client";
                        case SERVER -> "server";
                        case UNIVERSAL -> "universal";
                    };
                })
                .orElse("universal");
    }
}
