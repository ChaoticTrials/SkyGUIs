package de.melanx.easyskyblockmanagement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import java.awt.Color;

public class TextHelper {

    public static final Color LIGHT_GRAY = new Color(212, 212, 212);
    public static final Color LIGHT_GREEN = new Color(0, 222, 0);
    public static final Color DARK_GREEN = new Color(39, 167, 73);
    public static final Color LIGHT_RED = new Color(252, 84, 84);

    public static String shorten(@Nonnull Font font, String name, int length) {
        String s = name;
        int k = 0;
        while (font.width(s) > length) {
            s = name.substring(0, name.length() - k).trim() + "...";
            k++;
        }

        return s;
    }

    public static int stringLength(String s) {
        Font font = Minecraft.getInstance().font;
        return font.width(s);
    }

    public static int stringLength(FormattedText s) {
        Font font = Minecraft.getInstance().font;
        return font.width(s);
    }

    public static int stringLength(FormattedCharSequence s) {
        Font font = Minecraft.getInstance().font;
        return font.width(s);
    }
}
