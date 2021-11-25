package de.melanx.easyskyblockmanagement.client.screen.edit;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.PlayerListScreen;
import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.YouSureScreen;
import de.melanx.easyskyblockmanagement.client.widget.SizeableCheckbox;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Set;
import java.util.UUID;

public class InvitablePlayersScreen extends PlayerListScreen {

    private static final Component INVITE = ComponentBuilder.text("invite");

    private final Team team;
    private final BaseScreen prev;
    private Button inviteButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;

    public InvitablePlayersScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), InvitablePlayersScreen.getInvitablePlayers(), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.inviteButton = this.addRenderableWidget(new Button(this.x(10), this.y(200), 40, 20, INVITE, (button -> {
            Set<UUID> removalIds = this.getSelectedIds();
            ForgeHooksClient.pushGuiLayer(Minecraft.getInstance(), new YouSureScreen(ComponentBuilder.text("you_sure_invite", removalIds.size()), () -> {
                EasySkyblockManagement.getNetwork().handleKickPlayers(this.team.getName(), removalIds);
                //noinspection ConstantConditions
                if (removalIds.contains(Minecraft.getInstance().player.getGameProfile().getId())) {
                    Minecraft.getInstance().setScreen(new AllTeamsScreen());
                } else {
                    Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this.prev));
                }
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

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.font.draw(poseStack, ComponentBuilder.text("selected_amount", this.selectedAmount), this.x(28), this.y(35), Color.DARK_GRAY.getRGB());
    }

    public void updateButtons() {
        Set<UUID> selectedIds = this.getSelectedIds();
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

    private static Set<UUID> getInvitablePlayers() {
        Set<UUID> ids = Sets.newHashSet();

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return ids;
        }

        SkyblockSavedData data = SkyblockSavedData.get(level);
        for (UUID id : player.connection.getOnlinePlayerIds()) {
            if (!data.hasPlayerTeam(id)) {
                ids.add(id);
            }
        }

        return ids;
    }
}
