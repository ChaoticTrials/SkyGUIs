package de.melanx.skyguis.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyguis.SkyGUIs;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.render.ClientTickHandler;

import javax.annotation.Nonnull;

public class LoadingCircle extends GuiComponent implements Widget {

    private static final ResourceLocation ICONS = new ResourceLocation(SkyGUIs.getInstance().modid, "textures/gui/icons.png");
    private static final int TEXTURE_SIZE = 16;
    protected int width;
    protected int height;
    public int x;
    public int y;
    protected boolean active;

    public LoadingCircle(int x, int y, int size) {
        this(x, y, size, size);
    }

    public LoadingCircle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.active) {
            RenderSystem.setShaderTexture(0, ICONS);
            int state = (ClientTickHandler.ticksInGame / 2) % 8 * TEXTURE_SIZE;
            poseStack.pushPose();
            poseStack.scale(this.width / (float) TEXTURE_SIZE, this.height / (float) TEXTURE_SIZE, 1);
            this.blit(poseStack, this.x * TEXTURE_SIZE / this.width, this.y * TEXTURE_SIZE / this.height, state, 32, 16, 16);
            poseStack.popPose();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }
}
