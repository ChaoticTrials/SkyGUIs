package de.melanx.skyguis.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.render.ClientTickHandler;

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
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.highlight) {
            this.renderOutline(guiGraphics, Color.GREEN.getRGB());
        } else if (!this.valid) {
            this.renderOutline(guiGraphics, Color.RED.getRGB());
        }
    }

    public void renderOutline(GuiGraphics guiGraphics, int color) {
        guiGraphics.fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y, color);
        guiGraphics.fill(this.x - 1, this.y - 1, this.x, this.y + this.height + 1, color);
        guiGraphics.fill(this.x + this.width, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, color);
        guiGraphics.fill(this.x - 1, this.y + this.height, this.x + this.width + 1, this.y + this.height + 1, color);
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
        if (ClientTickHandler.ticksInGame() % 5 == 0) {
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
