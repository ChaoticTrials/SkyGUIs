package de.melanx.easyskyblockmanagement.client.screen.edit;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.TextHelper;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.widget.ScrollbarWidget;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.util.Math2;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class TeamMembersScreen extends BaseScreen {

    public static final int ENTRIES = 15;
    private static final int TOP_ENTRY = 32;

    private final Team team;
    private final BaseScreen prev;
    private final List<GameProfile> members;
    private ScrollbarWidget scrollbar;

    public TeamMembersScreen(Team team, BaseScreen prev) {
        super(new TextComponent(TextHelper.shorten(Minecraft.getInstance().font, team.getName(), 225)), 245, 250);
        this.team = team;
        this.prev = prev;

        List<GameProfile> tempProfiles = Lists.newArrayList();
        for (UUID id : team.getPlayers()) {
            GameProfile profile = GameProfileCache.get(id);
            if (profile != null) {
                tempProfiles.add(profile);
            }
        }

        this.members = tempProfiles.stream().sorted(Comparator.comparing(profile -> profile.getName().toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }

    @Override
    protected void init() {
        this.scrollbar = new ScrollbarWidget(this, this.xSize - 20, TOP_ENTRY - 3, 12, ENTRIES * 12);

        this.addRenderableWidget(new Button(this.relX + (this.xSize - this.font.width(PREV_SCREEN_COMPONENT) - 10) / 2, this.relY + this.ySize - 30, this.font.width(PREV_SCREEN_COMPONENT) + 10, 20, PREV_SCREEN_COMPONENT, button -> {
            Minecraft.getInstance().setScreen(this.prev);
        }));
        this.updateScrollbar();
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.scrollbar.render(poseStack);
        this.renderTitle(poseStack);

        int j = 0;
        for (int i = this.scrollbar.getOffset(); i < this.members.size(); i++) {
            if (j >= ENTRIES) break;
            GameProfile profile = this.members.get(i);
            String name = TextHelper.shorten(this.font, profile.getName(), 175);
            TextComponent teamNameComponent = new TextComponent(name);
            int y = this.relY + TOP_ENTRY + j * 12;
            this.font.draw(poseStack, teamNameComponent, this.relX + 10, y, TextHelper.DARK_GREEN.getRGB());
            boolean inBounds = Math2.isInBounds(this.relX + 10, y, this.font.width(teamNameComponent.getVisualOrderText()), 11, mouseX, mouseY);
            if (inBounds) {
                ArrayList<Component> tooltips = Lists.newArrayList(new TextComponent(profile.getName()));
                //noinspection ConstantConditions
                if (this.minecraft.options.advancedItemTooltips) {
                    tooltips.add(new TextComponent(profile.getId().toString()).withStyle(ChatFormatting.GRAY));
                }
                this.renderTooltip(poseStack, tooltips, Optional.empty(), mouseX, mouseY);
            }
            j++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scrollbar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        double x = mouseX - this.relX;
        double y = mouseY - this.relY;
        int entries = Math.min(ENTRIES, this.members.size());
        if (Math2.isInBounds(10, TOP_ENTRY, 175, entries * 12, x, y)) {
            int index = (int) ((y - TOP_ENTRY) / 12) + this.scrollbar.getOffset();
            GameProfile profile = this.members.get(index);
            if (Math2.isInBounds(10, TOP_ENTRY, this.font.width(profile.getName()), entries * 12, x, y)) {
                Minecraft.getInstance().setScreen(new ChatScreen(ModList.get().isLoaded("minemention")
                        ? "@" + profile.getName() + " "
                        : "/tell " + profile.getName() + " "));
            }
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
        this.scrollbar.setEnabled(this.members.size() > ENTRIES);
        this.scrollbar.setMaxOffset(this.members.size() - ENTRIES);
    }
}
