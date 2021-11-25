package de.melanx.easyskyblockmanagement.util;

import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentBuilder {

    private static String prefix() {
        return "screen." + EasySkyblockManagement.getInstance().modid + ".";
    }

    public static MutableComponent raw(String s, Object... args) {
        return new TranslatableComponent(ComponentBuilder.prefix() + s, args);
    }

    public static MutableComponent text(String s, Object... args) {
        return new TranslatableComponent(ComponentBuilder.prefix() + "text." + s, args);
    }

    public static MutableComponent title(String s, Object... args) {
        return new TranslatableComponent(ComponentBuilder.prefix() + "title." + s, args);
    }

    public static MutableComponent button(String s, Object... args) {
        return new TranslatableComponent(ComponentBuilder.prefix() + "button." + s, args);
    }
}
