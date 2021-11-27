package de.melanx.easyskyblockmanagement.client.screen.edit;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.PlayerListScreen;
import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.YouSureScreen;
import de.melanx.easyskyblockmanagement.client.widget.SizeableCheckbox;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamPlayersScreen extends PlayerListScreen {

    private final Team team;
    private final BaseScreen prev;
    private Button kickButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;

    public TeamPlayersScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), team.getPlayers(), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.kickButton = this.addRenderableWidget(new Button(this.x(10), this.y(200), 40, 20, ComponentBuilder.text("kick"), (button -> {
            Set<UUID> removalIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
            ForgeHooksClient.pushGuiLayer(Minecraft.getInstance(), new YouSureScreen(ComponentBuilder.text("you_sure_kick"), () -> {
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
                    TeamPlayersScreen.this.selectAll();
                } else {
                    TeamPlayersScreen.this.unselectAll();
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
        Set<UUID> selectedIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
        this.kickButton.active = selectedIds.size() > 0;
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
}
