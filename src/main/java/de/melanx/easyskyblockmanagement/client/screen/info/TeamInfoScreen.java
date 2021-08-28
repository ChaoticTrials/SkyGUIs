package de.melanx.easyskyblockmanagement.client.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nonnull;
import java.awt.*;

public class TeamInfoScreen extends BaseScreen {

    private static final MutableComponent MEMBERS_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.members");

    private final Team team;
    private final BaseScreen prev;

    public TeamInfoScreen(Team team, BaseScreen prev) {
        super(new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".title.team_info"), 300, 230);
        this.team = team;
        this.prev = prev;
    }

    @Override
    protected void init(GuiScreenEvent.InitGuiEvent event) {

    }

    @Override
    public void tick() {
        if (ClientTickHandler.ticksInGame % 200 == 0) {
            Minecraft.getInstance().setScreen(this.prev);
        }
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.font.draw(poseStack, this.team.getName(), this.relX + ((float) this.xSize / 2) - (float) this.font.width(this.team.getName()) / 2, this.relY + 10, Color.DARK_GRAY.getRGB());

        // Members
        this.font.draw(poseStack, MEMBERS_COMPONENT.append(":"), this.relX + 10, this.relY + 50, Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, String.valueOf(this.team.getPlayers().size()), this.relX + 10 + 50, this.relY + 50, Color.DARK_GRAY.getRGB());
    }
}
