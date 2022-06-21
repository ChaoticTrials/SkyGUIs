package de.melanx.skyguis.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyguis.SkyGUIs;
import de.melanx.skyguis.client.TemplateRenderer;
import de.melanx.skyguis.client.screen.base.LoadingResultHandler;
import de.melanx.skyguis.client.screen.notification.InformationScreen;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.ComponentBuilder;
import de.melanx.skyguis.util.LoadingResult;
import de.melanx.skyguis.util.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;

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
        String original = this.templates.get(this.currIndex);
        String shortened = TextHelper.shorten(this.font, original, 110);
        this.enableTooltip = !shortened.equals(original);
        this.currTemplate = original;
        Button templateButton = new Button(this.x(65), this.y(60), 122, 20, Component.literal(shortened), button -> {
            this.currIndex++;
            if (this.currIndex >= this.templates.size()) {
                this.currIndex = 0;
            }

            String orig = this.templates.get(this.currIndex);
            String s = TextHelper.shorten(this.font, orig, 110);
            this.enableTooltip = !s.equals(orig);
            this.currTemplate = orig;
            button.setMessage(Component.literal(s));
        }, (button, poseStack, mouseX, mouseY) -> {
            if (this.enableTooltip) {
                this.renderTooltip(poseStack, Component.literal(this.currTemplate), mouseX, mouseY);
            }
        });
        if (this.templates.size() == 1) {
            templateButton.active = false;
        }
        this.currTemplate = this.templates.get(this.currIndex);
        this.addRenderableWidget(templateButton);

        this.addRenderableWidget(new Button(this.x(27), this.y(92), 60, 20, CREATE, button -> {
            if (this.name.getValue().isBlank()) {
                this.name.setFocus(true);
                this.name.setValue(NameGenerator.randomName(new Random()));
            } else {
                SkyGUIs.getNetwork().handleCreateTeam(this.name.getValue(), this.currTemplate);
                //noinspection ConstantConditions
                this.getLoadingCircle().setActive(true);
            }
        }));
        this.addRenderableWidget(new Button(this.x(106), this.y(92), 60, 20, ABORT, button -> this.onClose()));
    }

    @Override
    public void tick() {
        this.name.tick();
        super.tick();
    }

    @Override
    public LoadingCircle createLoadingCircle() {
        return new LoadingCircle(this.centeredX(32), this.centeredY(32), 32);
    }

    @Override
    public void render_(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render_(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);
        this.font.draw(poseStack, NAME_COMPONENT, this.x(10), this.y(37), Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, TEMPLATE_COMPONENT, this.x(10), this.y(67), Color.DARK_GRAY.getRGB());
        if (!this.structureCache.containsKey(this.currTemplate)) {
            SkyGUIs.getNetwork().requestTemplateFromServer(this.currTemplate);
            this.structureCache.put(this.currTemplate, null);
            return;
        }

        TemplateRenderer renderer = this.structureCache.get(this.currTemplate);
        if (renderer != null) {
            renderer.render(poseStack, this.width / 6, this.centeredY(0));
        }
    }

    @Override
    public void onLoadingResult(LoadingResult result) {
        switch (result.status()) {
            case SUCCESS -> this.onClose();
            case FAIL -> {
                Minecraft minecraft = Minecraft.getInstance();
                ForgeHooksClient.pushGuiLayer(minecraft, new InformationScreen(result.reason(), TextHelper.stringLength(result.reason()) + 30, 100, () -> {
                    ForgeHooksClient.popGuiLayer(minecraft);
                }));
            }
        }
    }

    public void addStructureToCache(String name, ConfiguredTemplate template) {
        this.structureCache.put(name, new TemplateRenderer(template.getTemplate(), 130));
    }
}
