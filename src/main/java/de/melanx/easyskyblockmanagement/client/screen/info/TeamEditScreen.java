package de.melanx.easyskyblockmanagement.client.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nonnull;

public class TeamEditScreen extends BaseScreen {

    private final Team team;
    private final BaseScreen prev;

    public TeamEditScreen(Team team, BaseScreen prev) {
        super(new TextComponent(team.getName()), 245, 250);
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init(GuiScreenEvent.InitGuiEvent event) {

    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTitle(poseStack);
    }
}
