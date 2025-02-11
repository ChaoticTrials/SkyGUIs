package de.melanx.skyguis.client.screen.info;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
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

public class TeamInfoScreen extends BaseScreen implements LoadingResultHandler {

    private static final MutableComponent CONFIG_SELF_MANAGEMENT = Component.translatable("skyblockbuilder.command.disabled.join_request").withStyle(ChatFormatting.RED);
    private static final MutableComponent TEAM_JOIN_REQUESTS = Component.translatable("skyblockbuilder.command.disabled.team_join_request").withStyle(ChatFormatting.RED);
    private static final MutableComponent CONFIG_ALLOW_VISITS = Component.translatable("skyblockbuilder.command.disabled.team_visit").withStyle(ChatFormatting.RED);
    private static final MutableComponent TEAM_ALLOW_VISITS = Component.translatable("skyblockbuilder.command.disabled.visit_team").withStyle(ChatFormatting.RED);
    private static final MutableComponent USER_HAS_TEAM = Component.translatable("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED);
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
        Button joinButton = Button.builder(this.alreadySentJoinRequest() ? REQUESTED_TO_JOIN : REQUEST_TO_JOIN, button -> {
                    SkyGUIs.getNetwork().requestToJoinTeam(this.team);
                    this.getLoadingCircle().setActive(true);
                })
                .bounds(this.x(10), this.y(30), 110, 20)
                .build();
        //noinspection DataFlowIssue
        if (SkyblockSavedData.get(Minecraft.getInstance().level).hasPlayerTeam(Minecraft.getInstance().player)) {
            joinButton.setTooltip(Tooltip.create(USER_HAS_TEAM));
        } else if (!PermissionsConfig.selfManage) {
            joinButton.setTooltip(Tooltip.create(CONFIG_SELF_MANAGEMENT));
        } else if (!this.team.allowsJoinRequests()) {
            joinButton.setTooltip(Tooltip.create(TEAM_JOIN_REQUESTS));
        }
        joinButton.active = PermissionsConfig.selfManage && this.team.allowsJoinRequests() && !this.alreadySentJoinRequest() && !SkyblockSavedData.get(Minecraft.getInstance().level).hasPlayerTeam(Minecraft.getInstance().player);
        this.addRenderableWidget(joinButton);

        this.visitButton = Button.builder(VISIT_TEAM, button -> {
                    SkyGUIs.getNetwork().visitTeam(this.team);
                    this.getLoadingCircle().setActive(true);
                })
                .bounds(this.x(125), this.y(30), 110, 20)
                .build();
        //noinspection DataFlowIssue
        if (!this.minecraft.player.hasPermissions(1)) {
            if (!PermissionsConfig.Teleports.allowVisits) {
                this.visitButton.setTooltip(Tooltip.create(CONFIG_ALLOW_VISITS));
            } else if (!this.team.allowsVisits()) {
                this.visitButton.setTooltip(Tooltip.create(TEAM_ALLOW_VISITS));
            }
        }
        this.visitButton.active = this.minecraft.player.hasPermissions(1) || (PermissionsConfig.Teleports.allowVisits && this.team.allowsVisits());
        this.addRenderableWidget(this.visitButton);

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(10), this.y(55), 226, 20)
                .build());
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTitle(guiGraphics);
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        Minecraft minecraft = Minecraft.getInstance();
        switch (result.status()) {
            case SUCCESS -> {
                minecraft.screen = null;
                //noinspection ConstantConditions
                minecraft.player.sendSystemMessage(result.reason());
            }
            case FAIL ->
                    minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
        }
    }

    @Override
    public void tick() {
        super.tick();

        ClientLevel level = Minecraft.getInstance().level;
        //noinspection DataFlowIssue
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Player player = Minecraft.getInstance().player;

        //noinspection DataFlowIssue
        if (!(player.hasPermissions(1) || (PermissionsConfig.Teleports.allowVisits && this.team.allowsVisits()))) {
            return;
        }
        SkyMeta metaInfo = data.getOrCreateMetaInfo(player);
        if (metaInfo.canVisit(level.getGameTime())) {
            this.visitButton.setTooltip(null);
            this.visitButton.active = true;
            return;
        }

        this.visitButton.setTooltip(Tooltip.create(Component.translatable("skyblockbuilder.command.error.cooldown",
                RandomUtility.formattedCooldown(PermissionsConfig.Teleports.visitCooldown - (level.getGameTime() - metaInfo.getLastVisitTeleport())))));
        this.visitButton.active = false;
    }

    private boolean alreadySentJoinRequest() {
        LocalPlayer player = Minecraft.getInstance().player;

        return player != null && this.team.getJoinRequests().contains(player.getUUID());
    }
}
