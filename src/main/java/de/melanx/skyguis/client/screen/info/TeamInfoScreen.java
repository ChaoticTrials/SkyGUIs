package de.melanx.skyguis.client.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;

public class TeamInfoScreen extends BaseScreen {

    private static final MutableComponent CONFIG_SELF_MANAGEMENT = new TranslatableComponent("skyblockbuilder.command.disabled.join_request").withStyle(ChatFormatting.RED);
    private static final MutableComponent TEAM_JOIN_REQUESTS = new TranslatableComponent("skyblockbuilder.command.disabled.team_join_request").withStyle(ChatFormatting.RED);
    private static final MutableComponent CONFIG_ALLOW_VISITS = new TranslatableComponent("skyblockbuilder.command.disabled.team_visit").withStyle(ChatFormatting.RED);
    private static final MutableComponent TEAM_ALLOW_VISITS = new TranslatableComponent("skyblockbuilder.command.disabled.visit_team").withStyle(ChatFormatting.RED);
    private static final MutableComponent USER_HAS_TEAM = new TranslatableComponent("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED);
    private static final MutableComponent VISIT_TEAM = ComponentBuilder.text("visit_team");
    private static final MutableComponent REQUEST_TO_JOIN = ComponentBuilder.text("request_to_join");
    private static final MutableComponent REQUESTED_TO_JOIN = ComponentBuilder.text("requested_to_join").withStyle(ChatFormatting.GREEN);

    private final Team team;
    private final BaseScreen prev;

    public TeamInfoScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), 245, 85);
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        Button joinButton = new Button(this.x(10), this.y(30), 110, 20, this.alreadySentJoinRequest() ? REQUESTED_TO_JOIN : REQUEST_TO_JOIN, button -> {
        }, (button, poseStack, mouseX, mouseY) -> {
            //noinspection ConstantConditions
            if (SkyblockSavedData.get(Minecraft.getInstance().level).hasPlayerTeam(Minecraft.getInstance().player)) {
                this.renderTooltip(poseStack, USER_HAS_TEAM, mouseX, mouseY);
            } else if (!ConfigHandler.Utility.selfManage) {
                this.renderTooltip(poseStack, CONFIG_SELF_MANAGEMENT, mouseX, mouseY);
            } else if (!this.team.allowsJoinRequests()) {
                this.renderTooltip(poseStack, TEAM_JOIN_REQUESTS, mouseX, mouseY);
            }
        });
        joinButton.active = ConfigHandler.Utility.selfManage && this.team.allowsJoinRequests() && !this.alreadySentJoinRequest();
        this.addRenderableWidget(joinButton);

        Button visitButton = new Button(this.x(125), this.y(30), 110, 20, VISIT_TEAM, button -> {
        }, (button, poseStack, mouseX, mouseY) -> {
            if (!ConfigHandler.Utility.Teleports.allowVisits) {
                this.renderTooltip(poseStack, CONFIG_ALLOW_VISITS, mouseX, mouseY);
            } else if (!this.team.allowsVisits()) {
                this.renderTooltip(poseStack, TEAM_ALLOW_VISITS, mouseX, mouseY);
            }
        });
        visitButton.active = ConfigHandler.Utility.Teleports.allowVisits && this.team.allowsVisits();
        this.addRenderableWidget(visitButton);

        this.addRenderableWidget(new Button(this.x(10), this.y(55), 226, 20, PREV_SCREEN_COMPONENT, button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);
    }

    private boolean alreadySentJoinRequest() {
        LocalPlayer player = Minecraft.getInstance().player;

        return player != null && this.team.getJoinRequests().contains(player.getUUID());
    }
}
