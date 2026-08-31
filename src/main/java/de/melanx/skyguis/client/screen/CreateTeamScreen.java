package de.melanx.skyguis.client.screen;

import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.client.screens.ChoosePaletteScreen;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.template.TemplatePreview;
import de.melanx.skyblockbuilder.template.TemplatePreviewRenderer;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.*;

public class CreateTeamScreen extends BaseScreen {

    private static final Identifier SELECT_PALETTE = Identifier.withDefaultNamespace("textures/gui/sprites/widget/page_forward.png");
    private static final Component NAME_COMPONENT = ComponentBuilder.text("name");
    private static final Component TEMPLATE_COMPONENT = ComponentBuilder.raw("template");
    private static final Component SETTINGS_COMPONENT = ComponentBuilder.text("settings");
    private static final Component CREATE = ComponentBuilder.button("create");
    private static final Component ABORT = ComponentBuilder.button("abort");
    private static final Component TITLE = ComponentBuilder.title("create_team");
    private static final Component ALLOW_VISITS = ComponentBuilder.text("allow_visits");
    private static final Component ALLOW_REQUESTS = ComponentBuilder.text("allow_requests");
    private static final int TEMPLATE_BUTTON_WIDTH = 122;

    private transient final Map<String, TemplatePreviewRenderer> renderStructureCache = new HashMap<>();
    private transient final Map<String, ConfiguredTemplate> structureCache = new HashMap<>();
    private final List<String> templates;
    private final RegistryAccess registryAccess;
    private String currTemplate;
    private EditBox name;
    private int currIndex = 0;
    private boolean enableTooltip;
    private Button templateButton;
    private SizeableCheckbox allowVisits;
    private SizeableCheckbox allowJoinRequests;
    private Optional<Integer> paletteIndex = Optional.empty();

    public CreateTeamScreen() {
        super(TITLE, 200, 147);
        this.templates = TemplateLoader.getTemplateNames();
        this.registryAccess = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new CreateTeamScreen());
    }

    @Override
    protected void init() {
        this.renderStructureCache.clear();
        this.name = new EditBox(this.font, this.x(66), this.y(30), 120, 20, Component.empty());
        this.name.setValue(this.name.getValue());
        this.name.setMaxLength(Team.MAX_NAME_LENGTH);
        this.addRenderableWidget(this.name);

        if (this.templates.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendSystemMessage(ComponentBuilder.text("empty_templates").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
            return;
        }

        Component shortened = this.setCurrentTemplateAndGetShortenedName();
        this.templateButton = Button.builder(shortened, button -> {})
                .bounds(this.x(65), this.y(60), TEMPLATE_BUTTON_WIDTH, 20)
                .build(builder -> new Button.Plain(builder) {

                    @Override
                    public void onPress(@Nonnull InputWithModifiers input) {
                        CreateTeamScreen self = CreateTeamScreen.this;
                        self.currIndex = Mth.positiveModulo(self.currIndex + (input.hasShiftDown() ? -1 : 1), self.templates.size());

                        CreateTeamScreen.this.resetPaletteIndex();
                        this.setMessage(self.setCurrentTemplateAndGetShortenedName());
                        self.updateTemplateButton();
                    }
                });

        this.allowVisits = new SizeableCheckbox(this.x(65), this.y(85), 10, false);
        this.allowJoinRequests = new SizeableCheckbox(this.x(65), this.y(100), 10, false);

        this.addRenderableWidget(this.allowVisits);
        this.addRenderableWidget(this.allowJoinRequests);

        this.updateTemplateButton();
        if (this.templates.size() == 1) {
            this.templateButton.active = false;
        }
        this.currTemplate = this.templates.get(this.currIndex);
        this.addRenderableWidget(this.templateButton);

        this.addRenderableWidget(Button.builder(CREATE, button -> {
            if (this.name.getValue().isBlank()) {
                this.name.setFocused(true);
                this.name.setValue(NameGenerator.randomName(new Random()));
            } else {
                SkyGUIs.getNetwork().handleCreateTeam(this.name.getValue().strip(), this.currTemplate, this.paletteIndex, this.allowVisits.selected, this.allowJoinRequests.selected);
            }
        }).bounds(this.x(27), this.y(116), 60, 20).build());
        this.addRenderableWidget(Button.builder(ABORT, button -> this.onClose()).bounds(this.x(106), this.y(116), 60, 20).build());
    }

    private Component setCurrentTemplateAndGetShortenedName() {
        String orig = this.templates.get(this.currIndex);
        ConfiguredTemplate configuredTemplate = TemplateLoader.getConfiguredTemplate(orig);
        if (configuredTemplate == null) {
            throw new IllegalStateException("Templates not synced between client and server: " + orig);
        }

        Component nameComponent = configuredTemplate.getNameComponent();
        String s = TextHelper.shorten(this.font, nameComponent.getString(), 110);
        String desc = configuredTemplate.getDescriptionComponent().getString();
        this.enableTooltip = !s.equals(nameComponent.getString()) || !desc.isBlank();
        this.currTemplate = orig;

        return Component.literal(s).setStyle(nameComponent.getStyle());
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        this.renderTitle(graphics);
        graphics.text(this.font, NAME_COMPONENT, this.x(10), this.y(37), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, TEMPLATE_COMPONENT, this.x(10), this.y(67), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, SETTINGS_COMPONENT, this.x(10), this.y(92), Color.DARK_GRAY.getRGB(), false);
        if (!this.renderStructureCache.containsKey(this.currTemplate)) {
            SkyGUIs.getNetwork().requestTemplateFromServer(this.currTemplate);
            this.renderStructureCache.put(this.currTemplate, null);
            return;
        }

        TemplatePreviewRenderer renderer = this.renderStructureCache.get(this.currTemplate);
        if (renderer != null) {
            renderer.render(graphics);
        }

        float scale = 0.9f;
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(this.font, ALLOW_VISITS, (int) (this.x(82) / scale), (int) (this.y(87) / scale), Color.DARK_GRAY.getRGB(), false);
        graphics.text(this.font, ALLOW_REQUESTS, (int) (this.x(82) / scale), (int) (this.y(102) / scale), Color.DARK_GRAY.getRGB(), false);
        graphics.pose().scale(1 / 0.8f, 1 / 0.8f);
        graphics.pose().popMatrix();

        ConfiguredTemplate configuredTemplate = this.structureCache.get(this.currTemplate);
        if (configuredTemplate != null && configuredTemplate.canSelectPalette()) {
            int textureX = this.templateButton.x + this.templateButton.getWidth();
            int textureY = this.templateButton.y + 3;

            graphics.blit(RenderPipelines.GUI_TEXTURED, SELECT_PALETTE, textureX, textureY, 0, 0, 23, 13, 23, 13);

            if (this.isMouseOverPaletteSelection(configuredTemplate, textureX, textureY, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(this.font, SkyComponents.SCREEN_SELECT_PALETTE, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        boolean ret = super.mouseClicked(event, doubleClick);

        if (!ret) {
            ConfiguredTemplate configuredTemplate = this.structureCache.get(this.currTemplate);
            if (configuredTemplate != null && this.isMouseOverPaletteSelection(configuredTemplate, this.templateButton.x + this.templateButton.getWidth() + 5, this.templateButton.y, event.x(), event.y())) {
                Minecraft.getInstance().pushGuiLayer(
                        new ChoosePaletteScreen(configuredTemplate, this.registryAccess, index -> this.setPaletteIndex(configuredTemplate, index), this::resetPaletteIndex)
                );

                return true;
            }
        }

        return ret;
    }

    public void resetPaletteIndex() {
        this.paletteIndex = Optional.empty();
        this.renderStructureCache.remove(this.currTemplate);
    }

    public void setPaletteIndex(ConfiguredTemplate template, int index) {
        this.paletteIndex = Optional.of(index);
        this.renderStructureCache.put(this.currTemplate, new TemplatePreviewRenderer(
                new TemplatePreview(template),
                this.createArea(),
                this.registryAccess,
                index
        ));
    }

    private boolean isMouseOverPaletteSelection(ConfiguredTemplate template, int textureX, int textureY, double mouseX, double mouseY) {
        if (!template.canSelectPalette()) {
            return false;
        }

        return mouseX >= textureX && mouseX <= textureX + 23 && mouseY >= textureY && mouseY <= textureY + 13;
    }

    public void addStructureToCache(String name, ConfiguredTemplate template) {
        this.renderStructureCache.put(name, new TemplatePreviewRenderer(new TemplatePreview(template), this.createArea()));
        this.structureCache.put(name, template);
    }

    public void updateTemplateButton() {
        ConfiguredTemplate configuredTemplate = TemplateLoader.getConfiguredTemplate(this.currTemplate);

        if (configuredTemplate == null) {
            throw new IllegalStateException("Template does not exist: " + this.currTemplate);
        }

        this.templateButton.setWidth(configuredTemplate.canSelectPalette() ? TEMPLATE_BUTTON_WIDTH - 25 : TEMPLATE_BUTTON_WIDTH);

        if (this.enableTooltip) {
            MutableComponent nameComponent = configuredTemplate.getNameComponent().copy();
            MutableComponent descComponent = configuredTemplate.getDescriptionComponent().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            this.templateButton.setTooltip(Tooltip.create(nameComponent.append("\n").append(descComponent)));
        } else {
            this.templateButton.setTooltip(null);
        }
    }

    private TemplatePreviewRenderer.Area createArea() {
        return new TemplatePreviewRenderer.Area((int) (this.x(0) * 0.05), 0, (int) (this.x(0) * 0.9), this.height);
    }
}
