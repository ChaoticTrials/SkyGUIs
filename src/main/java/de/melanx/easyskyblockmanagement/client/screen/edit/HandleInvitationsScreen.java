package de.melanx.easyskyblockmanagement.client.screen.edit;

import com.google.common.collect.Lists;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.TextHelper;
import de.melanx.easyskyblockmanagement.client.screen.base.LoadingResultHandler;
import de.melanx.easyskyblockmanagement.client.screen.base.PlayerListScreen;
import de.melanx.easyskyblockmanagement.client.screen.base.TeamListScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.InformationScreen;
import de.melanx.easyskyblockmanagement.client.screen.notification.YouSureScreen;
import de.melanx.easyskyblockmanagement.client.widget.LoadingCircle;
import de.melanx.easyskyblockmanagement.client.widget.sizable.SizableButton;
import de.melanx.easyskyblockmanagement.network.handler.AnswerInvitation;
import de.melanx.easyskyblockmanagement.util.ComponentBuilder;
import de.melanx.easyskyblockmanagement.util.LoadingResult;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.widget.TextWidget;
import io.github.noeppi_noeppi.libx.screen.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class HandleInvitationsScreen extends TeamListScreen implements LoadingResultHandler {

    public static final Component TITLE = ComponentBuilder.title("invitations");

    public HandleInvitationsScreen() {
        super(TITLE, HandleInvitationsScreen.collectInvited(), 200, 230,
                new PlayerListScreen.ScrollbarInfo(180, 10, 210),
                new PlayerListScreen.RenderAreaInfo(10, 50, 160));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new HandleInvitationsScreen());
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
    protected void fillWidgets() {
        Minecraft minecraft = Minecraft.getInstance();
        for (int i = 0; i < this.values.size(); i++) {
            this.renderArea.addRenderableWidget2(new JoinTeamWidget(this.values.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12,
                    // pressing join button
                    team -> {
                        ForgeHooksClient.pushGuiLayer(minecraft, new YouSureScreen(ComponentBuilder.text("you_sure_join", team.getName()), () -> {
                            EasySkyblockManagement.getNetwork().handleInvitationAnswer(team.getName(), AnswerInvitation.Type.ACCEPT);
                            ForgeHooksClient.clearGuiLayers(minecraft);
                        }
                        ));
                    },
                    // pressing ignore button
                    team -> {
                        ForgeHooksClient.pushGuiLayer(minecraft, new YouSureScreen(ComponentBuilder.text("you_sure_ignore", team.getName()), () -> {
                            EasySkyblockManagement.getNetwork().handleInvitationAnswer(team.getName(), AnswerInvitation.Type.IGNORE);
                            Minecraft.getInstance().setScreen(new HandleInvitationsScreen());
                        }));
                    }));
        }
    }

    private static List<Team> collectInvited() {
        Minecraft minecraft = Minecraft.getInstance();
        //noinspection ConstantConditions
        SkyblockSavedData data = SkyblockSavedData.get(minecraft.level);
        //noinspection ConstantConditions
        List<Team> invites = data.getInvites(minecraft.player.getGameProfile().getId());
        if (invites == null) {
            return Lists.newArrayList();
        }

        return invites;
    }

    private static class JoinTeamWidget extends Panel {

        public JoinTeamWidget(Team team, Screen screen, int x, int y, int width, int height, Consumer<Team> onJoin, Consumer<Team> onIgnore) {
            super(screen, x, y, width, height);
            this.addRenderableWidget(new SizableButton(0, 0, 30, height, new TextComponent("Join"), button -> {
                onJoin.accept(team);
            }));
            this.addRenderableWidget(new SizableButton(33, 0, 30, height, new TextComponent("Ignore"), button -> {
                onIgnore.accept(team);
            }));
            this.addRenderableOnly(new TextWidget(screen, 66, 0, Math.min(width, TextHelper.stringLength(team.getName())), height, new TextComponent(team.getName()), Lists.newArrayList()));
        }
    }
}
