package de.melanx.skyguis.client.screen.edit;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.base.list.PlayerListScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.client.widget.sizable.SizeableCheckbox;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvitablePlayersScreen extends PlayerListScreen implements LoadingResultHandler {

    private static final Component INVITE = ComponentBuilder.text("invite");

    private final Team team;
    private final BaseScreen prev;
    private Button inviteButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;

    public InvitablePlayersScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), InvitablePlayersScreen.getInvitablePlayers(team), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.inviteButton = this.addRenderableWidget(new Button(this.x(10), this.y(200), 40, 20, INVITE, (button -> {
            Set<UUID> inviteIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
            ForgeHooksClient.pushGuiLayer(Minecraft.getInstance(), new YouSureScreen(ComponentBuilder.text("you_sure_invite", inviteIds.size()), () -> {
                SkyGUIs.getNetwork().handleInvitePlayers(this.team.getName(), inviteIds);
                Minecraft.getInstance().setScreen(new InvitablePlayersScreen(this.team, this.prev));
            }, () -> Minecraft.getInstance().setScreen(this)));
        })));

        this.addRenderableWidget(new Button(this.x(57), this.y(200), 115, 20, PREV_SCREEN_COMPONENT, button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));

        this.selectAll = this.addRenderableWidget(new SizeableCheckbox(this.x(9), this.y(32), 14, false, (checkbox, poseStack, mouseX, mouseY) -> {
            this.renderTooltip(poseStack, this.allSelected() ? UNSELECT_ALL : SELECT_ALL, mouseX, mouseY);
        }) {
            @Override
            public void onPress() {
                super.onPress();
                if (this.selected) {
                    InvitablePlayersScreen.this.selectAll();
                } else {
                    InvitablePlayersScreen.this.unselectAll();
                }
            }
        });

        super.init();
        this.updateButtons();
    }

    @Nullable
    @Override
    public LoadingCircle createLoadingCircle() {
        return new LoadingCircle(this.centeredX(32), this.centeredY(32), 32);
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        Minecraft minecraft = Minecraft.getInstance();
        ForgeHooksClient.pushGuiLayer(minecraft, new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, () -> {
            ForgeHooksClient.popGuiLayer(minecraft);
        }));
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.font.draw(poseStack, ComponentBuilder.text("selected_amount", this.selectedAmount), this.x(28), this.y(35), Color.DARK_GRAY.getRGB());
    }

    public void updateButtons() {
        Set<UUID> selectedIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
        this.inviteButton.active = selectedIds.size() > 0;
        this.selectAll.selected = this.allSelected();
        this.selectedAmount = selectedIds.size();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        this.updateButtons();
        return ret;
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    private static Set<UUID> getInvitablePlayers(Team team) {
        Set<UUID> ids = Sets.newHashSet();

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return ids;
        }

        SkyblockSavedData data = SkyblockSavedData.get(level);
        for (UUID id : player.connection.getOnlinePlayerIds()) {
            if (!data.hasPlayerTeam(id) && !data.hasInviteFrom(team, id)) {
                ids.add(id);
            }
        }

        return ids;
    }
}
