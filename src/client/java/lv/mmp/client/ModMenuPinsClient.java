package lv.mmp.client;

import com.mojang.blaze3d.platform.InputConstants;
import lv.mmp.client.gui.PinsYaclScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModMenuPinsClient implements ClientModInitializer {

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("mod-menu-pins", "main")
	);

	private static KeyMapping OPEN_SCREEN_KEY;

	@Override
	public void onInitializeClient() {

		OPEN_SCREEN_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.mod-menu-pins.open_screen",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_HOME,
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(ModMenuPinsClient::onClientEndTick);
	}

	private static void onClientEndTick(Minecraft client) {
		while (OPEN_SCREEN_KEY.consumeClick()) {
			client.gui.setScreen(PinsYaclScreen.create(client.gui.screen()));
		}
	}
}
