package de.melanx.skyguis.client.screen.edit;

import com.google.common.collect.Lists;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.base.list.PlayerListScreen;
import de.melanx.skyguis.client.screen.base.list.TeamListScreen;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.screen.notification.YouSureScreen;
import de.melanx.skyguis.client.widget.sizable.SizableButton;
import de.melanx.skyguis.network.handler.AnswerInvitation;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.impl.config.gui.screen.widget.TextWidget;
import org.moddingx.libx.screen.Panel;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    @Override
    public void onLoadingResult(LoadingResult result) {
        if (result.status() == LoadingResult.Status.SUCCESS) {
            Minecraft.getInstance().setScreen(null);
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
    }

    @Override
    protected void fillWidgets() {
        Minecraft minecraft = Minecraft.getInstance();
        for (int i = 0; i < this.values.size(); i++) {
            this.renderArea.addRenderableWidget2(new JoinTeamWidget(this.values.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12,
                    // pressing join button
                    team -> {
                        minecraft.pushGuiLayer(new YouSureScreen(this, ComponentBuilder.text("you_sure_join", team.getName()), () -> {
                            SkyGUIs.getNetwork().handleInvitationAnswer(team.getName(), AnswerInvitation.Type.ACCEPT);
                            minecraft.setScreen(null);
                        }
                        ));
                    },
                    // pressing ignore button
                    team -> {
                        minecraft.pushGuiLayer(new YouSureScreen(this, ComponentBuilder.text("you_sure_ignore", team.getName()), () -> {
                            SkyGUIs.getNetwork().handleInvitationAnswer(team.getName(), AnswerInvitation.Type.IGNORE);
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
        List<UUID> invites = data.getInvites(minecraft.player.getGameProfile().getId());
        if (invites == null) {
            return Lists.newArrayList();
        }

        return invites.stream().map(data::getTeam).collect(Collectors.toList());
    }

    private static class JoinTeamWidget extends Panel {

        public JoinTeamWidget(Team team, Screen screen, int x, int y, int width, int height, Consumer<Team> onJoin, Consumer<Team> onIgnore) {
            super(x, y, width, height);
            this.addRenderableWidget(SizableButton.builder(Component.literal("Join"), button -> onJoin.accept(team))
                    .bounds(0, 0, 30, height)
                    .build());
            this.addRenderableWidget(SizableButton.builder(Component.literal("Ignore"), button -> onIgnore.accept(team))
                    .bounds(33, 0, 30, height)
                    .build());
            this.addRenderableOnly(new TextWidget(66, 0, Math.min(width, TextHelper.stringLength(team.getName())), height, Component.literal(team.getName()), Lists.newArrayList()));
        }
    }
}
