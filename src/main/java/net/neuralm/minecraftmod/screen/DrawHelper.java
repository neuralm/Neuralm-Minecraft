package net.neuralm.minecraftmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@SuppressWarnings("WeakerAccess")
public class DrawHelper {

    /**
     * Draw a colored line.
     *
     * @param startX    The start X
     * @param startY    The start Y
     * @param endX      The line's end X
     * @param endY      The line's end Y
     * @param lineWidth The width of the line
     * @param color     The color to draw
     */
    public static void drawLine(double startX, double startY, double endX, double endY, float lineWidth, Color color) {
        RenderSystem.pushMatrix();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(lineWidth);

        bufferbuilder.pos(startX, startY, 0)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .endVertex();
        bufferbuilder.pos(endX, endY, 0)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .endVertex();

        tessellator.draw();

        RenderSystem.popMatrix();
    }

    /**
     * Draw a textured rectangle.
     * This method allows you to select a rectangle in the texture to draw.
     * This method expects the rectangle in the texture to be the same size as the rectangle to draw.
     *
     * @param x             The top left corner's x position
     * @param y             The top left corner's y position
     * @param z             The z level to draw on, higher means closer to the camera and thus on top of things drawn at a lower z.
     * @param textureX      The top left corner of the location in the texture
     * @param textureY      The top left corner of the rectangle in the texture to draw.
     * @param width         The width of the rectangle
     * @param height        The height of the rectangle
     * @param textureWidth  The width of the texture to draw
     * @param textureHeight The height of the texture to draw
     * @param color         The color to draw
     */
    public static void drawRectangle(int x, int y, int z, int textureX, int textureY, int width, int height,
                                     int textureWidth, int textureHeight, Color color) {
        BufferBuilder bufferbuilder = Tessellator.getInstance()
                                                 .getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        RenderSystem.enableDepthTest();

        /*
         *     A------B
         *     |      |
         *     |      |
         *     D------C
         */

        //A
        bufferbuilder.pos(x, y + height, z)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .tex(textureX / (float) textureWidth, (textureY + height) / (float) height)
                     .endVertex();

        //B
        bufferbuilder.pos(x + width, y + height, z)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .tex((textureX + textureWidth) / (float) width, (textureY + height) / (float) height)
                     .endVertex();

        //C
        bufferbuilder.pos(x + width, y, z)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .tex((textureX + textureWidth) / (float) width, 0)
                     .endVertex();

        //D
        bufferbuilder.pos(x, y, z)
                     .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                     .tex(textureX / (float) textureWidth, textureY / (float) textureHeight)
                     .endVertex();


        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    /**
     * Draws a textured rectangle.
     * This method draws the entire texture. To only draw part of it use {@link DrawHelper#drawRectangle(int, int, int, int, int, int, int, int, int, Color)}
     *
     * @param x      The top left corner's x position
     * @param y      The top left corner's y position
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param color  The color to draw at.
     */
    public static void drawRectangle(int x, int y, int width, int height, Color color) {
        drawRectangle(x, y, 1, 0, 0, width, height, width, height, color);
    }
}
