package de.melanx.skyguis.util;

import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ToggleButtons {

    private static final Component ALLOWED = ComponentBuilder.text("allowed").withStyle(ChatFormatting.GREEN);
    private static final Component DISALLOWED = ComponentBuilder.text("disallowed").withStyle(ChatFormatting.RED);

    public static void toggleState(Team team, Button button, MutableComponent base, Type type) {
        switch (type) {
            case VISITS -> team.setAllowVisit(!team.allowsVisits());
            case JOIN_REQUEST -> team.setAllowJoinRequest(!team.allowsJoinRequests());
        }
        button.setTooltip(Tooltip.create(base.copy().append(getStatus(team, type) ? ALLOWED : DISALLOWED)));
        SkyGUIs.getNetwork().toggleState(team, type);
    }

    public static boolean getStatus(Team team, Type type) {
        return switch (type) {
            case VISITS -> team.allowsVisits();
            case JOIN_REQUEST -> team.allowsJoinRequests();
        };
    }

    public enum Type {
        VISITS,
        JOIN_REQUEST
    }
}
