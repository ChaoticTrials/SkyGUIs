package de.melanx.easyskyblockmanagement.client.screen.info;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.screen.widget.ScrollbarWidget;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import io.github.noeppi_noeppi.libx.util.ScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AllTeamsScreen extends BaseScreen {

    public static final int ENTRIES = 15;
    private static final MutableComponent TEAMS_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.teams").setStyle(Style.EMPTY.withBold(true));
    private static final MutableComponent MEMBERS_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.members").setStyle(Style.EMPTY.withBold(true));
    private final List<Team> teams;
    private ScrollbarWidget scrollbar;

    public AllTeamsScreen() {
        super(new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".title.all_teams"), 200, 230);
        //noinspection ConstantConditions
        this.teams = SkyblockSavedData.get(Minecraft.getInstance().level).getTeams().stream()
                .filter(team -> !team.getName().isEmpty())
                .filter(team -> !team.isSpawn())
                .sorted(Comparator.comparing((team -> team.getName().toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList());
    }

    @Override
    protected void init(GuiScreenEvent.InitGuiEvent event) {
        this.scrollbar = new ScrollbarWidget(this, this.xSize - 20, 33, 12, this.ySize - 45);
        this.updateScrollbar();
    }

    @Override
    public void tick() {
        // peepoHappy
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        RenderHelper.renderGuiBackground(poseStack, this.relX, this.relY, this.xSize, this.ySize);
        this.scrollbar.render(poseStack);
        RenderSystem.setShaderColor(1, 0, 1, 0.5F);
        this.font.draw(poseStack, TEAMS_COMPONENT, this.relX + 10, this.relY + 13, Color.DARK_GRAY.getRGB());
        int memberLength = this.font.width(MEMBERS_COMPONENT.getVisualOrderText());
        this.font.draw(poseStack, MEMBERS_COMPONENT, this.relX + 179 - memberLength, this.relY + 13, Color.DARK_GRAY.getRGB());
        int j = 0;
        for (int i = this.scrollbar.getOffset(); i < this.teams.size(); i++) {
            if (j >= ENTRIES) break;
            Team team = this.teams.get(i);
            String name = team.getName();
            String s = name;
            int k = 0;
            int width = 175 - memberLength;
            while (this.font.width(s) > width) {
                s = name.substring(0, name.length() - k) + "...";
                k++;
            }

            TextComponent playerSizeComponent = new TextComponent(String.valueOf(team.getPlayers().size()));
            TextComponent teamNameComponent = new TextComponent(s);
            float x = this.relX + 179 - (float) memberLength / 2 - (float) this.font.width(playerSizeComponent.getVisualOrderText()) / 2;
            int y = this.relY + 37 + j * 12;
            this.font.draw(poseStack, teamNameComponent.setStyle(Style.EMPTY.applyFormat(team.isEmpty() ? ChatFormatting.RED : ChatFormatting.DARK_GREEN)), this.relX + 10, y, Color.DARK_GRAY.getRGB());
            this.font.draw(poseStack, playerSizeComponent, x, y, Color.DARK_GRAY.getRGB());
            j++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scrollbar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        mouseX -= this.relX;
        mouseY -= this.relY;

        int entries = Math.min(ENTRIES, this.teams.size());
        if (ScreenHelper.inBounds(10, 37, 175, entries * 12, mouseX, mouseY)) {
            int index = (int) ((mouseY - 37) / 12);
            Team team = this.teams.get(index);
            // TODO open detailed team screen

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.scrollbar.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.scrollbar.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scrollbar.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void updateScrollbar() {
        this.scrollbar.setEnabled(this.teams.size() > ENTRIES);
        this.scrollbar.setMaxOffset(this.teams.size() - ENTRIES);
    }
}
