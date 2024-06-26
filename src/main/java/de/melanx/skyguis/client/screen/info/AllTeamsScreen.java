package de.melanx.skyguis.client.screen.info;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.CreateTeamScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.widget.ClickableText;
import de.melanx.skyguis.client.widget.ScrollbarWidget;
import de.melanx.skyguis.config.ClientConfig;
import de.melanx.skyguis.tooltip.SmallTextTooltip;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.Math2;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.*;
import java.util.function.Predicate;

public class AllTeamsScreen extends BaseScreen implements LoadingResultHandler {

    public static final int ENTRIES = 13;
    private static final Component TEAMS_COMPONENT = ComponentBuilder.text("teams").setStyle(Style.EMPTY.withBold(true));
    private static final Component MEMBERS_COMPONENT = ComponentBuilder.text("members").setStyle(Style.EMPTY.withBold(true));
    private static final Component YOUR_TEAM = ComponentBuilder.text("your_team");
    private static final Component CLICK_ME = ComponentBuilder.text("click_me").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
    private static final Component FILTER = ComponentBuilder.text("filter");
    private static final Component VISIT_FILTER = ComponentBuilder.text("filter.visits");
    private static final Component JOIN_REQUEST_FILTER = ComponentBuilder.text("filter.join_requests");
    private static final Component EMPTY_TEAMS_FILTER = ComponentBuilder.text("filter.empty_teams");
    private static final int LONGEST_FILTER_TITLE_LENGTH = Math.max(TextHelper.stringLength(VISIT_FILTER),
            Math.max(TextHelper.stringLength(JOIN_REQUEST_FILTER), TextHelper.stringLength(EMPTY_TEAMS_FILTER)));
    private final SkyblockSavedData data;
    private final List<Team> teams = new ArrayList<>();
    private final Team playerTeam;
    private ScrollbarWidget scrollbar;
    private Button yourTeamButton;
    private Button teleportHome;
    private Button teleportSpawn;

    private FilteredCheckbox visitAllowedTeams;
    private FilteredCheckbox joinRequestsAllowedTeams;
    private FilteredCheckbox hideEmptyTeams;
    private CollapsableText filterText;
    private CycleButton<SortOrder> sortOrderButton;
    private SizeableCheckbox invertSort;

    public AllTeamsScreen() {
        super(TEAMS_COMPONENT, 200, 230);
        //noinspection ConstantConditions
        this.data = SkyblockSavedData.get(Minecraft.getInstance().level);
        this.playerTeam = this.data.getTeamFromPlayer(Minecraft.getInstance().player);
    }

    public static void open() {
        SkyGUIs.getNetwork().updateSkyblockSavedData();
        Minecraft.getInstance().setScreen(new AllTeamsScreen());
    }

    @Override
    protected void init() {
        if (this.playerTeam == null) {
            this.addRenderableWidget(Button.builder(ComponentBuilder.title("create_team"), button -> {
                        //noinspection DataFlowIssue
                        this.minecraft.setScreen(new CreateTeamScreen());
                    })
                    .tooltip(Tooltip.create(BaseScreen.OPEN_NEW_SCREEN))
                    .bounds(this.x(10), this.y(199), 160, 20)
                    .build());
            this.yourTeamButton = null;

            this.teleportSpawn = this.addRenderableWidget(Button.builder(ComponentBuilder.button("teleport_spawn_long"), button -> {
                        SkyGUIs.getNetwork().teleportToTeam(SkyblockSavedData.SPAWN_ID);
                        //noinspection DataFlowIssue
                        this.getLoadingCircle().setActive(true);
                    })
                    .bounds(this.x(10), this.y(this.ySize + 5), this.xSize - 20, 20)
                    .build());
        } else {
            MutableComponent component = Component.literal(TextHelper.shorten(this.font, this.playerTeam.getName(), this.xSize - TextHelper.stringLength(YOUR_TEAM) - 40));
            this.yourTeamButton = this.addRenderableWidget(new ClickableText(this.x(15) + TextHelper.stringLength(YOUR_TEAM), this.y(207), TextHelper.DARK_GREEN.getRGB(), component, button -> {
                //noinspection DataFlowIssue
                this.minecraft.setScreen(new TeamEditScreen(this.playerTeam, this));
            }));

            this.teleportHome = this.addRenderableWidget(Button.builder(ComponentBuilder.button("teleport_home"), button -> {
                        SkyGUIs.getNetwork().teleportToTeam(this.playerTeam);
                        //noinspection DataFlowIssue
                        this.getLoadingCircle().setActive(true);
                    })
                    .bounds(this.x(10), this.y(this.ySize + 5), this.xSize / 2 - 15, 20)
                    .build());
            //noinspection DataFlowIssue
            this.teleportHome.active = this.minecraft.player.hasPermissions(1) || PermissionsConfig.Teleports.home;

            this.teleportSpawn = this.addRenderableWidget(Button.builder(ComponentBuilder.button("teleport_spawn"), button -> {
                        SkyGUIs.getNetwork().teleportToTeam(SkyblockSavedData.SPAWN_ID);
                        //noinspection DataFlowIssue
                        this.getLoadingCircle().setActive(true);
                    })
                    .bounds(this.x(5 + this.xSize / 2), this.y(this.ySize + 5), this.xSize / 2 - 15, 20)
                    .build());
        }
        //noinspection DataFlowIssue
        this.teleportSpawn.active = this.minecraft.player.hasPermissions(1) || PermissionsConfig.Teleports.spawn;

        int filterSectionStart = this.xSize + 7;
        this.filterText = this.addRenderableWidget(new CollapsableText(false, this.x(filterSectionStart), this.y(2), 18, FILTER));
        this.visitAllowedTeams = this.addRenderableWidget(new FilteredCheckbox(this.x(filterSectionStart), this.y(22), 10, this.visitAllowedTeams != null && this.visitAllowedTeams.selected));
        this.joinRequestsAllowedTeams = this.addRenderableWidget(new FilteredCheckbox(this.x(filterSectionStart), this.y(36), 10, this.joinRequestsAllowedTeams != null && this.joinRequestsAllowedTeams.selected));
        this.hideEmptyTeams = this.addRenderableWidget(new FilteredCheckbox(this.x(filterSectionStart), this.y(50), 10, this.hideEmptyTeams == null || this.hideEmptyTeams.selected));
        this.sortOrderButton = this.addRenderableWidget(CycleButton.builder(SortOrder::getName)
                .withValues(SortOrder.values())
                .withInitialValue(SortOrder.ALPHABETICAL)
                .create(this.x(filterSectionStart + 15), this.y(65), 120, 20, ComponentBuilder.button("filter.sort_by"), (button, value) -> this.updateTeams()));
        this.invertSort = this.addRenderableWidget(new FilteredCheckbox(this.x(filterSectionStart), this.y(70), 10, false, ComponentBuilder.button("filter.invert")));

        this.scrollbar = new ScrollbarWidget(this, this.xSize - 20, 33, 12, this.ySize - 45);
        this.updateTeams();
        this.updateScrollbar();
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderHelper.renderGuiBackground(guiGraphics, this.x(this.xSize), this.y(0),
                (this.filterText.isOpen() ? LONGEST_FILTER_TITLE_LENGTH + 15 : TextHelper.stringLength(this.filterText.getMessage())) + 14,
                this.filterText.isOpen() ? 95 : 22,
                BaseScreen.GENERIC, 128, 64, 4, 125, 4, 60);
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        this.scrollbar.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, Component.empty().append(TEAMS_COMPONENT).append(" (" + this.teams.size() + "/" + (this.data.getSpawnOption().isPresent() ? this.data.getTeams().size() - 1 : this.data.getTeams().size()) + ")"), this.x(10), this.y(13), Color.DARK_GRAY.getRGB(), false);
        int memberLength = this.font.width(MEMBERS_COMPONENT.getVisualOrderText());
        guiGraphics.drawString(this.font, MEMBERS_COMPONENT, this.x(179) - memberLength, this.y(13), Color.DARK_GRAY.getRGB(), false);

        if (this.filterText.isOpen()) {
            int filterSectionStart = this.x(this.xSize + 20);
            guiGraphics.drawString(this.font, VISIT_FILTER, filterSectionStart, this.y(23), Color.DARK_GRAY.getRGB(), false);
            guiGraphics.drawString(this.font, JOIN_REQUEST_FILTER, filterSectionStart, this.y(37), Color.DARK_GRAY.getRGB(), false);
            guiGraphics.drawString(this.font, EMPTY_TEAMS_FILTER, filterSectionStart, this.y(51), Color.DARK_GRAY.getRGB(), false);
        }

        if (this.playerTeam != null) {
            guiGraphics.hLine(this.x(8), this.x(this.xSize - 26), this.y(ENTRIES * 12 + 41), Color.GRAY.getRGB());
            guiGraphics.drawString(this.font, YOUR_TEAM, (this.x(10)), this.y(207), Color.DARK_GRAY.getRGB(), false);

            // need to render tooltip here to be on top of scrollbar
            if (this.yourTeamButton.isHovered) {
                this.renderTeamTooltip(guiGraphics, mouseX, mouseY, this.playerTeam);
            }
        }

        int j = 0;
        for (int i = this.scrollbar.getOffset(); i < this.teams.size(); i++) {
            if (j >= ENTRIES) break;
            Team team = this.teams.get(i);
            String name = team.getName();
            String s = TextHelper.shorten(this.font, name, 175 - memberLength);

            String playerSizeComponent = String.valueOf(team.getPlayers().size());
            Component teamNameComponent = Component.literal(s);
            float x = this.x(179 - (float) memberLength / 2 - (float) this.font.width(playerSizeComponent) / 2);
            int y = this.y(37 + j * 12);
            guiGraphics.drawString(this.font, teamNameComponent, this.x(10), y, team.isEmpty() ? TextHelper.LIGHT_RED.getRGB() : TextHelper.DARK_GREEN.getRGB(), false);
            guiGraphics.drawString(this.font, playerSizeComponent, x, y, Color.DARK_GRAY.getRGB(), false);
            boolean inBounds = Math2.isInBounds(this.x(10), y, this.font.width(teamNameComponent.getVisualOrderText()), 11, mouseX, mouseY);
            if (inBounds) {
                this.renderTeamTooltip(guiGraphics, mouseX, mouseY, team);
            }
            j++;
        }
    }

    private void renderTeamTooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, @Nonnull Team team) {
        List<Component> textLines = Lists.newArrayList(Component.literal(team.getName()), CLICK_ME);
        List<Component> smallTextLines = Lists.newArrayList();
        if (this.minecraft != null && this.minecraft.options.advancedItemTooltips) {
            smallTextLines.add(ComponentBuilder.text("team_id").append(": " + team.getId().toString()));
        }
        smallTextLines.add(ComponentBuilder.text("members").append(": " + team.getPlayers().size()));
        smallTextLines.add(ComponentBuilder.text("created_at").append(": " + ClientConfig.date.format(new Date(team.getCreatedAt()))));
        smallTextLines.add(ComponentBuilder.text("last_changed").append(": " + ClientConfig.date.format(new Date(team.getLastChanged()))));
        guiGraphics.renderTooltip(this.minecraft.font, textLines, Optional.of(new SmallTextTooltip(smallTextLines, Color.GRAY)), mouseX, mouseY);
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
                //noinspection DataFlowIssue
                if (team.hasPlayer(this.minecraft.player)) {
                    this.minecraft.setScreen(new TeamEditScreen(team, this));
                } else {
                    this.minecraft.setScreen(new TeamInfoScreen(team, this));
                }
            }

            return true;
        }

        return super.mouseClicked(mouseX + this.relX, mouseY + this.relY, button);
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

    @Override
    public void onLoadingResult(LoadingResult result) {
        switch (result.status()) {
            case SUCCESS -> this.onClose();
            case FAIL -> {
                //noinspection DataFlowIssue
                this.minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, this.minecraft::popGuiLayer));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.visitAllowedTeams != null && this.joinRequestsAllowedTeams != null && this.hideEmptyTeams != null && this.sortOrderButton != null && this.invertSort != null) {
            boolean showFilter = this.filterText.isOpen();
            this.visitAllowedTeams.visible = showFilter;
            this.joinRequestsAllowedTeams.visible = showFilter;
            this.hideEmptyTeams.visible = showFilter;
            this.sortOrderButton.visible = showFilter;
            this.invertSort.visible = showFilter;
        }

        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        Player player = Minecraft.getInstance().player;

        //noinspection DataFlowIssue
        if (player.hasPermissions(1)) {
            return;
        }

        SkyMeta metaInfo = this.data.getOrCreateMetaInfo(player);
        if (PermissionsConfig.Teleports.home && this.playerTeam != null) {
            if (metaInfo.canTeleportHome(level.getGameTime())) {
                this.teleportHome.setTooltip(null);
                this.teleportHome.active = true;
            } else {
                this.teleportHome.setTooltip(Tooltip.create(Component.translatable("skyblockbuilder.command.error.cooldown",
                        RandomUtility.formattedCooldown(PermissionsConfig.Teleports.homeCooldown - (level.getGameTime() - metaInfo.getLastHomeTeleport())))));
                this.teleportHome.active = false;
            }
        }

        if (PermissionsConfig.Teleports.spawn) {
            if (metaInfo.canTeleportSpawn(level.getGameTime())) {
                this.teleportSpawn.setTooltip(null);
                this.teleportSpawn.active = true;
            } else {
                this.teleportSpawn.setTooltip(Tooltip.create(Component.translatable("skyblockbuilder.command.error.cooldown",
                        RandomUtility.formattedCooldown(PermissionsConfig.Teleports.spawnCooldown - (level.getGameTime() - metaInfo.getLastSpawnTeleport())))));
                this.teleportSpawn.active = false;
            }
        }
    }

    private void updateTeams() {
        this.teams.clear();
        Predicate<Team> visitsAllowed = team -> {
            if (this.visitAllowedTeams == null) {
                return true;
            }

            return !this.visitAllowedTeams.selected || team.allowsVisits();
        };

        Predicate<Team> joinRequestsAllowed = team -> {
            if (this.joinRequestsAllowedTeams == null) {
                return true;
            }

            return !this.joinRequestsAllowedTeams.selected || team.allowsJoinRequests();
        };

        Predicate<Team> hideEmptyTeams = team -> {
            if (this.hideEmptyTeams == null) {
                return true;
            }

            return !this.hideEmptyTeams.selected || !team.isEmpty();
        };

        this.teams.addAll(this.data.getTeams().stream()
                .filter(team -> !team.getName().isEmpty())
                .filter(team -> !team.isSpawn())
                .filter(visitsAllowed)
                .filter(joinRequestsAllowed)
                .filter(hideEmptyTeams)
                .sorted(this.sortOrderButton.getValue().comparator(this.invertSort.selected))
                .toList());

        this.updateScrollbar();
        this.filterText.setAppendix("(" + this.countEnabledFilters() + ")");
    }

    public int countEnabledFilters() {
        int i = 0;
        if (this.visitAllowedTeams != null && this.visitAllowedTeams.selected) i++;
        if (this.joinRequestsAllowedTeams != null && this.joinRequestsAllowedTeams.selected) i++;
        if (this.hideEmptyTeams != null && this.hideEmptyTeams.selected) i++;

        return i;
    }

    public enum SortOrder {
        ALPHABETICAL(ComponentBuilder.button("filter.alphabetical"), Comparator.comparing(team -> team.getName().toLowerCase(Locale.ROOT))),
        CREATION_DATE(ComponentBuilder.button("filter.creation_date"), Comparator.comparing(Team::getCreatedAt)),
        LAST_MODIFIED(ComponentBuilder.button("filter.last_modified"), Comparator.comparing(Team::getLastChanged)),
        MEMBER_COUNT(ComponentBuilder.button("filter.member_count"), Comparator.comparing(team -> team.getPlayers().size()));

        private final Component name;
        private final Comparator<Team> comparator;

        SortOrder(Component name, Comparator<Team> comparator) {
            this.name = name;
            this.comparator = comparator;
        }

        public Component getName() {
            return this.name;
        }

        public Comparator<Team> comparator() {
            return this.comparator(false);
        }

        public Comparator<Team> comparator(boolean invert) {
            return invert ? this.comparator.reversed() : this.comparator;
        }
    }

    private class FilteredCheckbox extends SizeableCheckbox {

        public FilteredCheckbox(int x, int y, int size, boolean selected) {
            this(x, y, size, selected, Component.empty());
        }

        public FilteredCheckbox(int x, int y, int size, boolean selected, Component component) {
            super(x, y, size, selected, component);
        }

        @Override
        public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.visible) {
                super.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public void onPress() {
            super.onPress();
            AllTeamsScreen.this.updateTeams();
        }
    }

    private static class CollapsableText extends AbstractWidget {

        private boolean enabled;
        private Component appendix = Component.empty();

        public CollapsableText(boolean open, int x, int y, int height, Component message) {
            super(x, y, 0, height, message);
            this.enabled = open;
            this.width = TextHelper.stringLength(this.getMessage());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.enabled = !this.enabled;
        }

        @Override
        protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderHelper.resetColor();
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY() + ((this.height - 8) / 2), Color.DARK_GRAY.getRGB(), false);
        }

        @Override
        protected void updateWidgetNarration(@Nonnull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        @Nonnull
        @Override
        public Component getMessage() {
            return (this.enabled ? Component.literal("⬇  ") : Component.literal("➡ ")).append(super.getMessage()).append(this.appendix);
        }

        public boolean isOpen() {
            return this.enabled;
        }

        public void setAppendix(String appendix) {
            this.appendix = Component.literal(" " + appendix);
            this.width = TextHelper.stringLength(this.getMessage());
        }
    }
}
