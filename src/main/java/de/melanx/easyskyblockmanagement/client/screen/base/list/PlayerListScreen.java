package de.melanx.easyskyblockmanagement.client.screen.base.list;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerListScreen extends ListScreen<GameProfile> {

    public PlayerListScreen(Component title, Set<UUID> players, int xSize, int ySize, ScrollbarInfo scrollbarInfo, RenderAreaInfo renderAreaInfo) {
        this(title, players.stream().toList(), xSize, ySize, scrollbarInfo, renderAreaInfo);
    }

    public PlayerListScreen(Component title, List<UUID> values, int xSize, int ySize, ScrollbarInfo scrollbarInfo, RenderAreaInfo renderAreaInfo) {
        super(title, PlayerListScreen.collectProfiles(values), xSize, ySize, scrollbarInfo, renderAreaInfo);
    }

    @Override
    protected void fillWidgets() {
        for (int i = 0; i < this.values.size(); i++) {
            this.addCheckboxWidget(this.renderArea.addRenderableWidget2(new PlayerWidget(this.values.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12)));
        }
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    private static List<GameProfile> collectProfiles(List<UUID> ids) {
        List<GameProfile> tempProfiles = Lists.newArrayList();
        for (UUID id : ids) {
            GameProfile profile = GameProfileCache.get(id);
            if (profile != null) {
                tempProfiles.add(profile);
            }
        }

        return tempProfiles.stream()
                .sorted(Comparator.comparing(profile -> profile.getName().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    protected class PlayerWidget extends CheckboxTextWidget {

        public PlayerWidget(GameProfile profile, Screen screen, int x, int y, int width, int height) {
            super(profile, screen, x, y, width, height, PlayerWidget.buildTooltip(profile), new TextComponent(profile.getName()));
        }

        public UUID getId() {
            return this.value.getId();
        }

        private static List<Component> buildTooltip(GameProfile profile) {
            List<Component> tooltip = Lists.newArrayList(
                    new TextComponent(profile.getName())
            );
            if (Minecraft.getInstance().options.advancedItemTooltips) {
                tooltip.add(new TextComponent(profile.getId().toString()).withStyle(ChatFormatting.GRAY));
            }

            return tooltip;
        }
    }
}
