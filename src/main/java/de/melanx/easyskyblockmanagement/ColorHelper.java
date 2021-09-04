package de.melanx.easyskyblockmanagement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmlclient.gui.GuiUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorHelper {

    public static final Color LIGHT_GRAY = new Color(212, 212, 212);
    public static final Color LIGHT_GREEN = new Color(0, 222, 0);
    public static final Color DARK_GREEN = new Color(39, 167, 73);
    public static final Color LIGHT_RED = new Color(252, 84, 84);

    public static void drawHoveringText(PoseStack pStack, List<? extends FormattedText> textLines,
                                        List<String> smallTextLines, int mouseX,
                                        int mouseY, int screenWidth, int screenHeight, int backgroundColor,
                                        int borderColorStart, int borderColorEnd) {
        // Modification start
        int maxTextWidth = -1;
        Font font = Minecraft.getInstance().font;
        float textScale = Minecraft.getInstance().isEnforceUnicode() ? 1F : 0.7F;
        // Modification End

        if (!textLines.isEmpty()) {
            RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, textLines, pStack, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
            if (MinecraftForge.EVENT_BUS.post(event)) return;
            mouseX = event.getX();
            mouseY = event.getY();
            screenWidth = event.getScreenWidth();
            screenHeight = event.getScreenHeight();
            maxTextWidth = event.getMaxWidth();
            font = event.getFontRenderer();

            RenderSystem.disableDepthTest();
            int tooltipTextWidth = 0;

            for (FormattedText textLine : textLines) {
                int textLineWidth = font.width(textLine);
                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
            }

            // Modification start
            for (String smallText : smallTextLines) {
                int size = (int) (font.width(smallText) * textScale);

                if (size > tooltipTextWidth) {
                    tooltipTextWidth = size;
                }
            }
            // Modification end

            boolean needsWrap = false;

            int titleLinesCount = 1;
            int tooltipX = mouseX + 12;
            if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
                if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
                    if (mouseX > screenWidth / 2) {
                        tooltipTextWidth = mouseX - 12 - 8;
                    } else {
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    }
                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<FormattedText> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    FormattedText textLine = textLines.get(i);
                    List<FormattedText> wrappedLine = font.getSplitter().splitLines(textLine, tooltipTextWidth, Style.EMPTY);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (FormattedText line : wrappedLine) {
                        int lineWidth = font.width(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;

                if (mouseX > screenWidth / 2) {
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                } else {
                    tooltipX = mouseX + 12;
                }
            }

            int tooltipY = mouseY - 12;
            int tooltipHeight = 8;

            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount) {
                    tooltipHeight += 2; // gap between title lines and next lines
                }
            }

            // Modification start
            tooltipHeight += smallTextLines.size() * 10;
            // Modification end

            if (tooltipY < 4) {
                tooltipY = 4;
            } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 4;
            }

            final int zLevel = 400;
            RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, textLines, pStack, tooltipX, tooltipY, font, backgroundColor, borderColorStart, borderColorEnd);
            MinecraftForge.EVENT_BUS.post(colorEvent);
            backgroundColor = colorEvent.getBackground();
            borderColorStart = colorEvent.getBorderStart();
            borderColorEnd = colorEvent.getBorderEnd();

            pStack.pushPose();
            Matrix4f mat = pStack.last().pose();
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(mat, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, textLines, pStack, tooltipX, tooltipY, font, tooltipTextWidth, tooltipHeight));

            MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            pStack.translate(0.0D, 0.0D, zLevel);

            int tooltipTop = tooltipY;

            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                FormattedText line = textLines.get(lineNumber);
                if (line != null) {
                    font.drawInBatch(Language.getInstance().getVisualOrder(line), (float) tooltipX, (float) tooltipY, -1, true, mat, renderType, false, 0, 15728880);
                }

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }

            renderType.endBatch();
            pStack.popPose();

            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, textLines, pStack, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

            // Modification start
            int y = tooltipTop + tooltipHeight - 6;

            for (int i = smallTextLines.size() - 1; i >= 0; --i) {
                PoseStack smallPoseStack = new PoseStack();
                smallPoseStack.translate(0, 0, zLevel);
                smallPoseStack.scale(textScale, textScale, 1);

                MultiBufferSource.BufferSource renderTypeBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                font.drawInBatch(
                        ChatFormatting.GRAY + smallTextLines.get(i),
                        tooltipX / textScale,
                        (y - (Minecraft.getInstance().isEnforceUnicode() ? 2 : 0)) / textScale,
                        -1,
                        true,
                        smallPoseStack.last().pose(),
                        renderTypeBuffer,
                        false,
                        0,
                        15728880
                );

                renderTypeBuffer.endBatch();

                y -= 9;
            }
            // Modification end

            RenderSystem.enableDepthTest();
        }
    }
}
