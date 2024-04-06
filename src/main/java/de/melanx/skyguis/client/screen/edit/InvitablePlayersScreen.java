package de.melanx.skyguis.client.screen.edit;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.base.list.PlayerListScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.client.widget.sizable.SizeableCheckbox;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
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
        super(Component.literal(team.getName()), InvitablePlayersScreen.getInvitablePlayers(team), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.inviteButton = this.addRenderableWidget(Button.builder(INVITE, (button -> {
                    Set<UUID> inviteIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
                    Minecraft.getInstance().pushGuiLayer(new YouSureScreen(this, ComponentBuilder.text("you_sure_invite", inviteIds.size()), () -> {
                        SkyGUIs.getNetwork().handleInvitePlayers(this.team.getName(), inviteIds);
                        Minecraft.getInstance().setScreen(new InvitablePlayersScreen(this.team, this.prev));
                    }, () -> Minecraft.getInstance().setScreen(this)));
                }))
                .bounds(this.x(10), this.y(200), 40, 20)
                .build());

        this.addRenderableWidget(Button.builder(PREV_SCREEN_COMPONENT, button -> Minecraft.getInstance().setScreen(this.prev))
                .bounds(this.x(57), this.y(200), 115, 20)
                .build());

        this.selectAll = this.addRenderableWidget(new SizeableCheckbox(this.x(9), this.y(32), 14, false, this.allSelected() ? UNSELECT_ALL : SELECT_ALL) {
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
    public void onLoadingResult(LoadingResult result) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, ComponentBuilder.text("selected_amount", this.selectedAmount), this.x(28), this.y(35), Color.DARK_GRAY.getRGB(), false);
    }

    public void updateButtons() {
        Set<UUID> selectedIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
        this.inviteButton.active = !selectedIds.isEmpty();
        this.selectAll.selected = this.allSelected();
        this.selectedAmount = selectedIds.size();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        this.updateButtons();
        return ret;
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
