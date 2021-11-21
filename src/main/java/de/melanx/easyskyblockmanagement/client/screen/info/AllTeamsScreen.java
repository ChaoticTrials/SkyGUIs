package de.melanx.easyskyblockmanagement.client.screen.info;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.TextHelper;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.edit.TeamPlayersScreen;
import de.melanx.easyskyblockmanagement.client.widget.ScrollbarWidget;
import de.melanx.easyskyblockmanagement.config.ClientConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.util.Math2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fmlclient.gui.GuiUtils;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class AllTeamsScreen extends BaseScreen {

    public static final int ENTRIES = 15;
    private static final MutableComponent TEAMS_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.teams").setStyle(Style.EMPTY.withBold(true));
    private static final MutableComponent MEMBERS_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.members").setStyle(Style.EMPTY.withBold(true));
    private final List<Team> teams;
    private ScrollbarWidget scrollbar;

    public AllTeamsScreen() {
        super(TEAMS_COMPONENT, 200, 230);
        //noinspection ConstantConditions
        this.teams = SkyblockSavedData.get(Minecraft.getInstance().level).getTeams().stream()
                .filter(team -> !team.getName().isEmpty())
                .filter(team -> !team.isSpawn())
                .sorted(Comparator.comparing((team -> team.getName().toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList());
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new AllTeamsScreen());
    }

    @Override
    protected void init() {
        this.scrollbar = new ScrollbarWidget(this, this.xSize - 20, 33, 12, this.ySize - 45);
        this.updateScrollbar();
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.scrollbar.render(poseStack);
        this.font.draw(poseStack, TEAMS_COMPONENT, this.x(10), this.y(13), Color.DARK_GRAY.getRGB());
        int memberLength = this.font.width(MEMBERS_COMPONENT.getVisualOrderText());
        this.font.draw(poseStack, MEMBERS_COMPONENT, this.x(179) - memberLength, this.y(13), Color.DARK_GRAY.getRGB());
        int j = 0;
        for (int i = this.scrollbar.getOffset(); i < this.teams.size(); i++) {
            if (j >= ENTRIES) break;
            Team team = this.teams.get(i);
            String name = team.getName();
            String s = TextHelper.shorten(this.font, name, 175 - memberLength);

            TextComponent playerSizeComponent = new TextComponent(String.valueOf(team.getPlayers().size()));
            TextComponent teamNameComponent = new TextComponent(s);
            float x = this.x(179 - (float) memberLength / 2 - (float) this.font.width(playerSizeComponent.getVisualOrderText()) / 2);
            int y = this.y(37 + j * 12);
            this.font.draw(poseStack, teamNameComponent, this.x(10), y, team.isEmpty() ? TextHelper.LIGHT_RED.getRGB() : TextHelper.DARK_GREEN.getRGB());
            this.font.draw(poseStack, playerSizeComponent, x, y, Color.DARK_GRAY.getRGB());
            boolean inBounds = Math2.isInBounds(this.x(10), y, this.font.width(teamNameComponent.getVisualOrderText()), 11, mouseX, mouseY);
            if (inBounds) {
                ArrayList<TextComponent> textLines = Lists.newArrayList(new TextComponent(name));
                ArrayList<String> smallTextLines = Lists.newArrayList();
                smallTextLines.add(I18n.get("screen.easyskyblockmanagement.text.members") + ": " + team.getPlayers().size());
                smallTextLines.add(I18n.get("screen.easyskyblockmanagement.text.created_at") + ": " + ClientConfig.date.format(new Date(team.getCreatedAt())));
                smallTextLines.add(I18n.get("screen.easyskyblockmanagement.text.last_changed") + ": " + ClientConfig.date.format(new Date(team.getLastChanged())));
                TextHelper.drawHoveringText(poseStack, textLines, smallTextLines, mouseX, mouseY, this.width, this.height,
                        GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, GuiUtils.DEFAULT_BORDER_COLOR_END);
            }
            j++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scrollbar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        mouseX -= this.relX;
        mouseY -= this.relY;

        int entries = Math.min(ENTRIES, this.teams.size());
        if (Math2.isInBounds(10, 37, 175, entries * 12, mouseX, mouseY)) {
            int index = (int) ((mouseY - 37) / 12) + this.scrollbar.getOffset();
            Team team = this.teams.get(index);
            if (Math2.isInBounds(10, 37, this.font.width(team.getName()), entries * 12, mouseX, mouseY)) {
                Minecraft.getInstance().setScreen(new TeamPlayersScreen(team, this));
                if (team.hasPlayer(Minecraft.getInstance().player)) {
                    Minecraft.getInstance().setScreen(new TeamEditScreen(team, this));
                } else {
                    Minecraft.getInstance().setScreen(new TeamInfoScreen(team, this));
                }
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.scrollbar.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.scrollbar.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scrollbar.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void updateScrollbar() {
        this.scrollbar.setEnabled(this.teams.size() > ENTRIES);
        this.scrollbar.setMaxOffset(this.teams.size() - ENTRIES);
    }
}
