package de.melanx.skyguis.util;

import de.melanx.skyguis.SkyGUIs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ComponentBuilder {

    private static String prefix() {
        return "screen." + SkyGUIs.getInstance().modid + ".";
    }

    public static MutableComponent raw(String s, Object... args) {
        return Component.translatable(ComponentBuilder.prefix() + s, args);
    }

    public static MutableComponent text(String s, Object... args) {
        return Component.translatable(ComponentBuilder.prefix() + "text." + s, args);
    }

    public static MutableComponent title(String s, Object... args) {
        return Component.translatable(ComponentBuilder.prefix() + "title." + s, args);
    }

    public static MutableComponent button(String s, Object... args) {
        return Component.translatable(ComponentBuilder.prefix() + "button." + s, args);
    }
}
