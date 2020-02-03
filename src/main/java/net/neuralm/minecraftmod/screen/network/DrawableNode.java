package net.neuralm.minecraftmod.screen.network;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.neuralm.client.neat.ConnectionGene;
import net.neuralm.client.neat.nodes.AbstractNode;
import net.neuralm.client.neat.nodes.InputNode;
import net.neuralm.client.neat.nodes.OutputNode;
import net.neuralm.minecraftmod.screen.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.neuralm.minecraftmod.screen.network.DrawableOrganism.NODE_TEXTURE_LOCATION;

public class DrawableNode {

    final List<ConnectionGene> inConnections = new ArrayList<>();
    final AbstractNode node;
    private final DrawableOrganism organism;
    private final int NODE_SIZE = 16;

    int layer;
    private double y;

    DrawableNode(double y, AbstractNode node, DrawableOrganism organism) {
        this.y = y;
        this.node = node;
        this.organism = organism;
    }

    /**
     * Set the layer of this node, if the given layer is bigger than its current layer the layer will be updated.
     * Will also call setLayer for all incoming nodes.
     *
     * @param layer The possibly new layer
     */
    void setLayer(int layer) {
        inConnections.forEach(gene -> organism.getNodeFromIdentifier(gene.inNodeIdentifier)
                                              .setLayer(layer + 1));
        this.layer = Math.max(this.layer, layer);
    }

    /**
     * Gets the (normalized) x value, where 0 is the left wall and 1 is the right wall at default zoom level.
     *
     * @return The normalized x position
     */
    private double getX() {
        return 1 - (1.0 / (organism.maxLayer) * layer);
    }

    /**
     * Gets the (normalized) y value, where 0 is the top wall and 1 is the bottom wall at default zoom level.
     *
     * @return The normalized y position
     */
    private double getY() {
        return y;
    }

    /**
     * Returns the x position in real pixel space.
     *
     * @param width The width of 1 screen on default zoom level
     * @return The x position
     */
    private int getX(int width) {
        return (int) (width * getX()) + NODE_SIZE;
    }

    /**
     * Returns the y position in real pixel space.
     *
     * @param height The height of 1 screen on default zoom level
     * @return The y position
     */
    private int getY(int height) {
        return (int) (height * getY());
    }

    /**
     * Draw the node, this will also draw the direct incoming connections.
     *
     * @param width  The width of 1 screen on default zoom level
     * @param height The height of 1 screen on default zoom level
     */
    void draw(int width, int height) {

        Minecraft.getInstance()
                 .getTextureManager()
                 .bindTexture(NODE_TEXTURE_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.enableTexture();

        //Color is based on type
        Color color;
        if (node instanceof InputNode) {
            color = new Color(0, 255, 0);
        } else if (node instanceof OutputNode) {
            color = new Color(255, 0, 0);
        } else {
            color = new Color(0, 0, 255);
        }

        DrawHelper.drawRectangle(getX(width), getY(height), NODE_SIZE, NODE_SIZE, color);

        //Draw all inconnections
        for (ConnectionGene gene : inConnections) {
            DrawableNode otherNode = organism.getNodeFromIdentifier(gene.inNodeIdentifier);

            //Start at the middle of the other node, and end on the middle of the current one
            DrawHelper.drawLine(otherNode.getX(width) + NODE_SIZE / 2f,
                                otherNode.getY(height) + NODE_SIZE / 2f,
                                getX(width) + NODE_SIZE / 2f,
                                getY(height) + NODE_SIZE / 2f,
                                (float) (Math.abs(gene.weight) * 10f), //Width is based on the weight.
                                Math.signum(
                                        gene.weight) == 1 ? Color.GREEN : Color.RED); //Red if negative green if positive
        }
    }
}
