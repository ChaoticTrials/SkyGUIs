package de.melanx.skyguis.client.screen.edit;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.base.list.PlayerListScreen;
import de.melanx.skyguis.client.screen.info.AllTeamsScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.client.widget.sizable.SizeableCheckbox;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamPlayersScreen extends PlayerListScreen implements LoadingResultHandler {

    private final Team team;
    private final BaseScreen prev;
    private Button kickButton;
    private Checkbox selectAll;
    private int selectedAmount = 0;
    private Runnable onSuccess;

    public TeamPlayersScreen(Team team, BaseScreen prev) {
        super(Component.literal(team.getName()), team.getPlayers(), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init() {
        this.kickButton = this.addRenderableWidget(new Button(this.x(10), this.y(200), 40, 20, ComponentBuilder.text("kick"), (button -> {
            Set<UUID> removalIds = this.getSelectedValues().stream().map(GameProfile::getId).collect(Collectors.toSet());
            Minecraft.getInstance().pushGuiLayer(new YouSureScreen(this, ComponentBuilder.text("you_sure_kick", removalIds.size()), () -> {
                SkyGUIs.getNetwork().handleKickPlayers(this.team.getName(), removalIds);
                this.onSuccess = () -> {
                    //noinspection DataFlowIssue
                    if (removalIds.contains(Minecraft.getInstance().player.getGameProfile().getId())) {
                        Minecraft.getInstance().setScreen(new AllTeamsScreen());
                    } else {
                        Minecraft.getInstance().setScreen(new TeamPlayersScreen(this.team, this.prev));
                    }
                };
            }));
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

    @Override
    public void onLoadingResult(LoadingResult result) {
        switch (result.status()) {
            case SUCCESS -> this.onSuccess.run();
            case FAIL -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
            }
        }
    }
}
