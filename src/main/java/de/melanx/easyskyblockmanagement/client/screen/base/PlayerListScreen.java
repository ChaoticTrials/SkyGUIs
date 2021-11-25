package de.melanx.easyskyblockmanagement.client.screen.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.client.screen.BaseScreen;
import de.melanx.easyskyblockmanagement.client.widget.RenderArea;
import de.melanx.easyskyblockmanagement.client.widget.ScrollbarWidget;
import de.melanx.easyskyblockmanagement.client.widget.SizeableCheckbox;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import io.github.noeppi_noeppi.libx.impl.config.gui.screen.widget.TextWidget;
import io.github.noeppi_noeppi.libx.screen.Panel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PlayerListScreen extends BaseScreen {

    private static final int ENTRY_HEIGHT = 14;

    private final ScrollbarInfo scrollbarInfo;
    private final RenderAreaInfo renderAreaInfo;
    private final Set<PlayerWidget> playerWidgets = new HashSet<>();
    protected RenderArea renderArea;
    protected final List<GameProfile> players;
    protected ScrollbarWidget scrollbar;

    // While rendering the scrollable view, tooltips must be delayed
    // Because scissors is enabled, and they need to be rendered with
    // absolute coordinates as they should not be cut by the screen border.
    private final List<Pair<PoseStack.Pose, Runnable>> capturedTooltips = new LinkedList<>();
    private boolean isCapturingTooltips = false;


    public PlayerListScreen(Component title, Set<UUID> players, int xSize, int ySize, ScrollbarInfo scrollbarInfo, RenderAreaInfo renderAreaInfo) {
        super(title, xSize, ySize);

        List<GameProfile> tempProfiles = Lists.newArrayList();
        for (UUID id : players) {
            GameProfile profile = GameProfileCache.get(id);
            if (profile != null) {
                tempProfiles.add(profile);
            }
        }

        this.players = tempProfiles.stream()
                .sorted(Comparator.comparing(profile -> profile.getName().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
        this.scrollbarInfo = scrollbarInfo;
        this.renderAreaInfo = renderAreaInfo;
    }

    @Override
    protected void init() {
        this.playerWidgets.clear();
        this.scrollbar = new ScrollbarWidget(this, this.scrollbarInfo.x, this.scrollbarInfo.y, 12, this.scrollbarInfo.height);
        this.renderArea = this.addWidget(new RenderArea(this, this.x(this.renderAreaInfo.x), this.y(this.renderAreaInfo.y), this.renderAreaInfo.width, this.entriesPerPage() * ENTRY_HEIGHT, this.xSize - 20, this.players.size() * ENTRY_HEIGHT, ENTRY_HEIGHT) {
            @Override
            public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                PlayerListScreen.this.isCapturingTooltips = true;
                super.render(poseStack, mouseX, mouseY, partialTick);
                PlayerListScreen.this.isCapturingTooltips = false;
                PlayerListScreen.this.capturedTooltips.forEach(pair -> {
                    int x = mouseX - this.getInitX();
                    int y = mouseY - this.getInitY();
                    if (x > 0 && x < this.getRenderWidth()
                            && y > 0 && y < this.getRenderHeight()) {
                        poseStack.pushPose();
                        poseStack.setIdentity();
                        poseStack.mulPoseMatrix(pair.getLeft().pose());
                        pair.getRight().run();
                        poseStack.popPose();
                    }
                });
                PlayerListScreen.this.capturedTooltips.clear();
            }
        });

        for (int i = 0; i < this.players.size(); i++) {
            this.playerWidgets.add(this.renderArea.addRenderableWidget2(new PlayerWidget(this.players.get(i), this, 0, ENTRY_HEIGHT * i, 100, 12)));
        }

        this.scrollbar.addListener(this.renderArea);
        this.updateScrollbar();
    }

    protected abstract int entriesPerPage();

    public boolean allSelected() {
        for (PlayerWidget widget : this.playerWidgets) {
            if (!widget.selected()) {
                return false;
            }
        }

        return true;
    }

    public void selectAll() {
        for (PlayerWidget widget : this.playerWidgets) {
            widget.checkbox.selected = true;
        }
    }

    public void unselectAll() {
        for (PlayerWidget widget : this.playerWidgets) {
            widget.checkbox.selected = false;
        }
    }

    public Set<UUID> getSelectedIds() {
        Set<UUID> ids = new HashSet<>();
        for (PlayerWidget widget : this.playerWidgets) {
            if (widget.selected()) {
                ids.add(widget.getId());
            }
        }

        return ids;
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);
        this.scrollbar.render(poseStack);
        this.renderArea.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.scrollbar.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
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
        this.scrollbar.setEnabled(this.players.size() > this.entriesPerPage());
        this.scrollbar.setMaxOffset(this.players.size() - this.entriesPerPage());
    }

    public List<GameProfile> getPlayers() {
        return ImmutableList.copyOf(this.players);
    }

    public static record ScrollbarInfo(int x, int y, int height) {
    }

    public static record RenderAreaInfo(int x, int y, int width, int height) {

        public RenderAreaInfo(int x, int y, int width) {
            this(x, y, width, 0);
        }
    }

    protected static class PlayerWidget extends Panel {

        private final GameProfile profile;
        private final Checkbox checkbox;

        public PlayerWidget(GameProfile profile, Screen screen, int x, int y, int width, int height) {
            super(screen, x, y, width, height);
            this.profile = profile;
            this.checkbox = new SizeableCheckbox(0, 0, height, false);
            this.addRenderableWidget(this.checkbox);
            List<Component> tooltip = Lists.newArrayList(
                    new TextComponent(profile.getName())
            );
            if (screen.getMinecraft().options.advancedItemTooltips) {
                tooltip.add(new TextComponent(profile.getId().toString()).withStyle(ChatFormatting.GRAY));
            }
            TextWidget textWidget = new TextWidget(screen, height + 5, 0, Math.min(width, screen.getMinecraft().font.width(profile.getName())), height,
                    new TextComponent(profile.getName()), tooltip);
            this.addRenderableWidget(textWidget);
        }

        public UUID getId() {
            return this.profile.getId();
        }

        public boolean selected() {
            return this.checkbox.selected();
        }
    }

    private void captureTooltip(PoseStack.Pose pose, Runnable action) {
        this.capturedTooltips.add(Pair.of(pose, action));
    }

    @Override
    protected void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull ItemStack stack, int x, int y) {
        if (this.isCapturingTooltips) {
            // Not inside lambda as the value may change
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, stack, x, y));
        } else {
            super.renderTooltip(poseStack, stack, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> special, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, special, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, special, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull Component tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, stack));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, stack);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltip, @Nonnull Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font, stack));
        } else {
            super.renderTooltip(poseStack, tooltip, tooltipComponent, x, y, font, stack);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, stack));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, stack);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nullable Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font);
        }
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltip, int mouseX, int mouseY, @Nullable Font font, @Nonnull ItemStack stack) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font, stack));
        } else {
            super.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, font, stack);
        }
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltip, int x, int y, @Nonnull Font font) {
        if (this.isCapturingTooltips) {
            this.captureTooltip(poseStack.last(), () -> this.renderTooltip(poseStack, tooltip, x, y, font));
        } else {
            super.renderTooltip(poseStack, tooltip, x, y, font);
        }
    }
}
