package de.melanx.skyguis.client.screen;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.template.TemplateRenderer;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateTeamScreen extends BaseScreen implements LoadingResultHandler {

    private static final Component NAME_COMPONENT = ComponentBuilder.text("name");
    private static final Component TEMPLATE_COMPONENT = ComponentBuilder.raw("template");
    private static final Component CREATE = ComponentBuilder.button("create");
    private static final Component ABORT = ComponentBuilder.button("abort");
    private static final Component TITLE = ComponentBuilder.title("create_team");

    private transient final Map<String, TemplateRenderer> structureCache = new HashMap<>();
    private final List<String> templates;
    private String currTemplate;
    private EditBox name;
    private int currIndex = 0;
    private boolean enableTooltip;
    private Button templateButton;

    public CreateTeamScreen() {
        super(TITLE, 200, 125);
        this.templates = TemplateLoader.getTemplateNames();
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new CreateTeamScreen());
    }

    @Override
    protected void init() {
        this.name = new EditBox(this.font, this.x(66), this.y(30), 120, 20, Component.empty());
        this.name.setMaxLength(Short.MAX_VALUE);
        this.name.setValue(this.name.getValue());
        this.name.setMaxLength(64);
        this.addRenderableWidget(this.name);

        if (this.templates.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendSystemMessage(ComponentBuilder.text("empty_templates").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
            return;
        }

        Component shortened = this.setCurrentTemplateAndGetShortenedName();
        this.templateButton = Button.builder(shortened, button -> {
                    this.currIndex++;
                    if (this.currIndex >= this.templates.size()) {
                        this.currIndex = 0;
                    }

                    Component s = this.setCurrentTemplateAndGetShortenedName();
                    button.setMessage(s);
                    this.updateTemplateButton();
                })
                .bounds(this.x(65), this.y(60), 122, 20)
                .build();

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
                SkyGUIs.getNetwork().handleCreateTeam(this.name.getValue().strip(), this.currTemplate);
                //noinspection ConstantConditions
                this.getLoadingCircle().setActive(true);
            }
        }).bounds(this.x(27), this.y(92), 60, 20).build());
        this.addRenderableWidget(Button.builder(ABORT, button -> this.onClose()).bounds(this.x(106), this.y(92), 60, 20).build());
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
    public void tick() {
        this.name.tick();
        super.tick();
    }

    @Override
    public void render_(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render_(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTitle(guiGraphics);
        guiGraphics.drawString(this.font, NAME_COMPONENT, this.x(10), this.y(37), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, TEMPLATE_COMPONENT, this.x(10), this.y(67), Color.DARK_GRAY.getRGB(), false);
        if (!this.structureCache.containsKey(this.currTemplate)) {
            SkyGUIs.getNetwork().requestTemplateFromServer(this.currTemplate);
            this.structureCache.put(this.currTemplate, null);
            return;
        }

        TemplateRenderer renderer = this.structureCache.get(this.currTemplate);
        if (renderer != null) {
            renderer.render(guiGraphics, this.width / 6, this.centeredY(0));
        }
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        switch (result.status()) {
            case SUCCESS -> this.onClose();
            case FAIL -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.pushGuiLayer(new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, minecraft::popGuiLayer));
            }
        }
    }

    public void addStructureToCache(String name, ConfiguredTemplate template) {
        this.structureCache.put(name, new TemplateRenderer(template.getTemplate(), 130));
    }

    public void updateTemplateButton() {
        if (this.enableTooltip) {
            ConfiguredTemplate configuredTemplate = TemplateLoader.getConfiguredTemplate(this.currTemplate);
            if (configuredTemplate == null) {
                throw new IllegalStateException("Template does not exist: " + this.currTemplate);
            }

            MutableComponent nameComponent = configuredTemplate.getNameComponent().copy();
            MutableComponent descComponent = configuredTemplate.getDescriptionComponent().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            this.templateButton.setTooltip(Tooltip.create(nameComponent.append("\n").append(descComponent)));
        }
    }
}
