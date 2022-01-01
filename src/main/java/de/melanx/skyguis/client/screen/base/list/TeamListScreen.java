package de.melanx.skyguis.client.screen.base.list;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class TeamListScreen extends ListScreen<Team> {

    public TeamListScreen(Component title, List<Team> teams, int xSize, int ySize, PlayerListScreen.ScrollbarInfo scrollbarInfo, PlayerListScreen.RenderAreaInfo renderAreaInfo) {
        super(title, teams, xSize, ySize, scrollbarInfo, renderAreaInfo);
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    @Override
    protected void fillWidgets() {
        for (int i = 0; i < this.values.size(); i++) {
            this.addCheckboxWidget(this.renderArea.addRenderableWidget2(new TeamWidget(this.values.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12)));
        }
    }

    protected class TeamWidget extends CheckboxTextWidget {

        public TeamWidget(Team team, Screen screen, int x, int y, int width, int height) {
            super(team, screen, x, y, width, height, TeamWidget.buildTooltip(team), new TextComponent(team.getName()));
        }

        private static List<Component> buildTooltip(Team team) {
            List<Component> tooltip = Lists.newArrayList(
                    new TextComponent(team.getName())
            );
            if (Minecraft.getInstance().options.advancedItemTooltips) {
                tooltip.add(new TextComponent(team.getId().toString()).withStyle(ChatFormatting.GRAY));
            }

            return tooltip;
        }
    }
}
