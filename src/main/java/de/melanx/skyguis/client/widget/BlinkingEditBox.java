package de.melanx.skyguis.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;

public class BlinkingEditBox extends EditBox {

    private int timer;
    private boolean highlight;
    private boolean valid = true;

    public BlinkingEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (this.highlight) {
            this.renderOutline(poseStack, Color.GREEN.getRGB());
        } else if (!this.valid) {
            this.renderOutline(poseStack, Color.RED.getRGB());
        }
    }

    public void renderOutline(PoseStack poseStack, int color) {
        fill(poseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y, color);
        fill(poseStack, this.x - 1, this.y - 1, this.x, this.y + this.height + 1, color);
        fill(poseStack, this.x + this.width, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, color);
        fill(poseStack, this.x - 1, this.y + this.height, this.x + this.width + 1, this.y + this.height + 1, color);
    }

    public void blink() {
        this.timer = 20;
    }

    public void setValid() {
        this.valid = true;
    }

    public void setInvalid() {
        this.valid = false;
    }

    @Override
    public void tick() {
        if (ClientTickHandler.ticksInGame % 5 == 0) {
            if (this.timer > 0) {
                this.highlight = !this.highlight;
            } else {
                this.highlight = false;
            }
        }
        if (this.timer > 0) {
            this.timer--;
        }
    }
}
