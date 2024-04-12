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
        this.addRenderableWidget(this.name);

        if (this.templates.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendSystemMessage(ComponentBuilder.text("empty_templates").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
            return;
        }

        String shortened = this.setCurrentTemplateAndGetShortenedName();
        Button templateButton = Button.builder(Component.literal(shortened), button -> {
                    this.currIndex++;
                    if (this.currIndex >= this.templates.size()) {
                        this.currIndex = 0;
                    }

                    String s = this.setCurrentTemplateAndGetShortenedName();
                    button.setMessage(Component.literal(s));
                })
                .bounds(this.x(65), this.y(60), 122, 20)
                .tooltip(Tooltip.create(Component.literal(this.currTemplate)))
                .build();
        if (this.enableTooltip) {
            templateButton.setTooltip(Tooltip.create(Component.literal(this.currTemplate)));
        }
        if (this.templates.size() == 1) {
            templateButton.active = false;
        }
        this.currTemplate = this.templates.get(this.currIndex);
        this.addRenderableWidget(templateButton);

        this.addRenderableWidget(Button.builder(CREATE, button -> {
            if (this.name.getValue().isBlank()) {
                this.name.setFocused(true);
                this.name.setValue(NameGenerator.randomName(new Random()));
            } else {
                SkyGUIs.getNetwork().handleCreateTeam(this.name.getValue(), this.currTemplate);
                //noinspection ConstantConditions
                this.getLoadingCircle().setActive(true);
            }
        }).bounds(this.x(27), this.y(92), 60, 20).build());
        this.addRenderableWidget(Button.builder(ABORT, button -> this.onClose()).bounds(this.x(106), this.y(92), 60, 20).build());
    }

    private String setCurrentTemplateAndGetShortenedName() {
        String orig = this.templates.get(this.currIndex);
        String s = TextHelper.shorten(this.font, orig, 110);
        ConfiguredTemplate configuredTemplate = TemplateLoader.getConfiguredTemplate(orig);
        if (configuredTemplate == null) {
            throw new IllegalStateException("Templates not synced between client and server: " + orig);
        }

        String desc = configuredTemplate.getDescriptionComponent().getString();
        this.enableTooltip = !s.equals(orig) || !desc.isBlank();
        this.currTemplate = orig;

        return s;
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
}
