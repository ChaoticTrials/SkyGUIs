package de.melanx.easyskyblockmanagement.client.screen.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.PlayerListScreen;
import de.melanx.easyskyblockmanagement.client.screen.info.AllTeamsScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.YouSureScreen;
import de.melanx.easyskyblockmanagement.client.widget.SizeableCheckbox;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Set;
import java.util.UUID;

// TODO TextComponents to TranslatableComponents
public class TeamPlayersScreen extends PlayerListScreen {

    private static final Component SELECT_ALL = new TextComponent("Select all");
    private static final Component UNSELECT_ALL = new TextComponent("Unselect all");
    private final Team team;
    private final BaseScreen prev;
    private Button kickButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;

    public TeamPlayersScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), team.getPlayers(), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 180));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.kickButton = this.addRenderableWidget(new Button(this.x(10), this.y(200), 40, 20, new TextComponent("Kick"), (button -> {
            Set<UUID> removalIds = this.getSelectedIds();
            ForgeHooksClient.pushGuiLayer(Minecraft.getInstance(), new YouSureScreen(new TextComponent("Do you really want to kick these " + removalIds.size() + " player(s)?"), () -> {
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
        this.font.draw(poseStack, new TextComponent(this.selectedAmount + " player(s) selected"), this.x(28), this.y(35), Color.DARK_GRAY.getRGB());
    }

    public void updateButtons() {
        Set<UUID> selectedIds = this.getSelectedIds();
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
