package de.melanx.skyguis;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static final Lazy<KeyMapping.Category> CATEGORY = Lazy.of(() -> KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(SkyGUIs.getInstance().modid, "key.categories." + SkyGUIs.getInstance().modid)
    ));

    public static final KeyMapping ALL_TEAMS = new KeyMapping(SkyGUIs.getInstance().modid + ".key.all_teams_screen", GLFW.GLFW_KEY_PERIOD, CATEGORY.get());
}
