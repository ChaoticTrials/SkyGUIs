package de.melanx.skyguis.client.screen.info;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class TeamInfoScreen extends BaseScreen {

    private static final MutableComponent CONFIG_SELF_MANAGEMENT = SkyComponents.DISABLED_JOIN_REQUEST;
    private static final MutableComponent TEAM_JOIN_REQUESTS = SkyComponents.DISABLED_TEAM_JOIN_REQUEST;
    private static final MutableComponent CONFIG_ALLOW_VISITS = SkyComponents.DISABLED_TEAM_VISIT;
    private static final MutableComponent TEAM_ALLOW_VISITS = SkyComponents.DISABLED_VISIT_TEAM;
    private static final MutableComponent USER_HAS_TEAM = SkyComponents.ERROR_USER_HAS_TEAM;
    private static final MutableComponent VISIT_TEAM = ComponentBuilder.text("visit_team");
    private static final MutableComponent REQUEST_TO_JOIN = ComponentBuilder.text("request_to_join");
    private static final MutableComponent REQUESTED_TO_JOIN = ComponentBuilder.text("requested_to_join").withStyle(ChatFormatting.GREEN);

    private final Team team;
    private final BaseScreen prev;
    private Button visitButton;

    public TeamInfoScreen(Team team, BaseScreen prev) {
        super(Component.literal(team.getName()), 245, 85);
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        Button joinButton = Button.builder(this.alreadySentJoinRequest() ? REQUESTED_TO_JOIN : REQUEST_TO_JOIN, button -> SkyGUIs.getNetwork().requestToJoinTeam(this.team))
                .bounds(this.x(10), this.y(30), 110, 20)
                .build();
        //noinspection DataFlowIssue
        if (SkyblockSavedData.get(Minecraft.getInstance().level).hasPlayerTeam(this.minecraft.player)) {
            joinButton.setTooltip(Tooltip.create(USER_HAS_TEAM));
        } else if (!PermissionManager.INSTANCE.hasPermission(this.minecraft.player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS)) {
            joinButton.setTooltip(Tooltip.create(CONFIG_SELF_MANAGEMENT));
        } else if (!this.team.allowsJoinRequests()) {
            joinButton.setTooltip(Tooltip.create(TEAM_JOIN_REQUESTS));
        }

        joinButton.active = PermissionManager.INSTANCE.hasPermission(this.minecraft.player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS) && this.team.allowsJoinRequests() && !this.alreadySentJoinRequest() && !SkyblockSavedData.get(Minecraft.getInstance().level).hasPlayerTeam(this.minecraft.player);
        this.addRenderableWidget(joinButton);

        this.visitButton = Button.builder(VISIT_TEAM, button -> SkyGUIs.getNetwork().teleportToTeam(this.team, SkyMeta.TeleportType.VISIT))
                .bounds(this.x(125), this.y(30), 110, 20)
                .build();
        //noinspection DataFlowIssue
        if (!this.minecraft.player.hasPermissions(1)) {
            if (!PermissionManager.INSTANCE.hasPermission(this.minecraft.player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND)) {
                this.visitButton.setTooltip(Tooltip.create(CONFIG_ALLOW_VISITS));
            } else if (!this.team.allowsVisits()) {
                this.visitButton.setTooltip(Tooltip.create(TEAM_ALLOW_VISITS));
            }
        }
        this.visitButton.active = PermissionManager.INSTANCE.hasPermission(this.minecraft.player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND) && (PermissionManager.INSTANCE.mayBypassLimitation(this.minecraft.player) || this.team.allowsVisits());
        this.addRenderableWidget(this.visitButton);

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(10), this.y(55), 226, 20)
                .build());
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTitle(guiGraphics);
    }

    @Override
    public void tick() {
        super.tick();

        ClientLevel level = Minecraft.getInstance().level;
        //noinspection DataFlowIssue
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Player player = this.minecraft.player;

        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND) && (PermissionManager.INSTANCE.mayBypassLimitation(player) || this.team.allowsVisits())) {
            return;
        }

        SkyMeta metaInfo = data.getOrCreateMetaInfo(player);
        if (metaInfo.canTeleport(SkyMeta.TeleportType.VISIT, level.getGameTime())) {
            this.visitButton.setTooltip(null);
            this.visitButton.active = true;
            return;
        }

        this.visitButton.setTooltip(Tooltip.create(SkyComponents.ERROR_COOLDOWN.apply(
                RandomUtility.formattedCooldown(PermissionsConfig.Teleports.Cooldowns.visitCooldown - (level.getGameTime() - metaInfo.getLastTeleport(SkyMeta.TeleportType.VISIT)))
        )));
        this.visitButton.active = false;
    }

    private boolean alreadySentJoinRequest() {
        LocalPlayer player = this.minecraft.player;

        return player != null && this.team.getJoinRequests().contains(player.getUUID());
    }
}
