package de.melanx.easyskyblockmanagement.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.EasySkyblockManagement;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public class CreateTeamScreen extends BaseScreen {

    private static final Component NAME_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".text.name");
    private static final Component TEMPLATE_COMPONENT = new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".template");
    private static final Component CREATE = new TranslatableComponent("screen.easyskyblockmanagement.button.create");
    private static final Component ABORT = new TranslatableComponent("skyblockbuilder.screen.button.abort");

    private final List<ConfiguredTemplate> templates;
    private String currTemplate;
    private EditBox name;
    private int currIndex = 0;
    private boolean enableTooltip;

    public CreateTeamScreen() {
        super(new TranslatableComponent("screen." + EasySkyblockManagement.getInstance().modid + ".title.create_team"), 200, 125);
        this.templates = TemplateLoader.getConfiguredTemplates();
    }

    @Override
    protected void init(GuiScreenEvent.InitGuiEvent event) {
        this.name = new EditBox(this.font, this.relX + 66, this.relY + 30, 120, 20, new TextComponent(""));
        this.name.setMaxLength(Short.MAX_VALUE);
        this.name.setValue(this.name.getValue());
        this.addRenderableWidget(this.name);

        this.addRenderableWidget(new Button(this.relX + 65, this.relY + 60, 122, 20, new TextComponent(this.templates.get(this.currIndex).getName()), button -> {
            this.currIndex++;
            if (this.currIndex >= this.templates.size()) {
                this.currIndex = 0;
            }

            String orig = this.templates.get(this.currIndex).getName();
            String s = orig;
            int i = 0;
            this.enableTooltip = false;
            while (this.font.width(s) > 110) {
                s = orig.substring(0, orig.length() - i) + "...";
                this.enableTooltip = true;
                i++;
            }
            this.currTemplate = orig;
            button.setMessage(new TextComponent(s));
        }, (button, poseStack, mouseX, mouseY) -> {
            if (this.enableTooltip) {
                this.renderTooltip(poseStack, new TextComponent(this.currTemplate), mouseX, mouseY);
            }
        }));

        this.addRenderableWidget(new Button(this.relX + 27, this.relY + 92, 60, 20, CREATE, button -> {
            EasySkyblockManagement.getNetwork().handleCreateTeam(this.name.getValue(), this.currTemplate);
            this.onClose();
        }));
        this.addRenderableWidget(new Button(this.relX + 106, this.relY + 92, 60, 20, ABORT, button -> this.onClose()));
    }

    @Override
    public void tick() {
        this.name.tick();
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.font.draw(poseStack, this.title, (float) (this.width / 2 - this.font.width(this.title.getVisualOrderText()) / 2), this.relY + 13, Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, NAME_COMPONENT, this.relX + 10, this.relY + 37, Color.DARK_GRAY.getRGB());
        this.font.draw(poseStack, TEMPLATE_COMPONENT, this.relX + 10, this.relY + 67, Color.DARK_GRAY.getRGB());
    }
}
