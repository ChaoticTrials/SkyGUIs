package de.melanx.easyskyblockmanagement.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.easyskyblockmanagement.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;

public class YouSureScreen extends BaseScreen {

    private final BaseScreen parent;
    private final OnConfirm onConfirm;
    private final OnAbort onAbort;

    public YouSureScreen(BaseScreen parent, Component component, OnConfirm onConfirm) {
        this(parent, component.copy().withStyle(ChatFormatting.RED), onConfirm, () -> {
            ForgeHooksClient.popGuiLayer(Minecraft.getInstance());
        });
    }

    public YouSureScreen(BaseScreen parent, Component component, OnConfirm onConfirm, OnAbort onAbort) {
        super(component, TextHelper.stringLength(component) + 30, 100);
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.onAbort = onAbort;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.x(10), this.y(50), 50, 20, new TextComponent("Confirm"), (button -> {
            this.onConfirm.onConfirm();
        })));
        this.addRenderableWidget(new Button(this.x(80), this.y(50), 50, 20, new TextComponent("Abort"), (button -> {
            this.onAbort.onAbort();
        })));
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(poseStack, this.parent.relX, this.parent.relY, this.parent.relX + this.parent.xSize - 1, this.parent.relY + this.parent.ySize - 1, -1072689136, -804253680);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTitle(poseStack);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE && this.minecraft != null) {
            this.minecraft.setScreen(null);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public interface OnConfirm {
        void onConfirm();
    }

    public interface OnAbort {
        void onAbort();
    }
}
