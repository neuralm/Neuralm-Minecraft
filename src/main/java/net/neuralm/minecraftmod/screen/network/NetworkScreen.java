package net.neuralm.minecraftmod.screen.network;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.neuralm.client.neat.Organism;
import net.neuralm.minecraftmod.Neuralm;
import org.lwjgl.opengl.GL11;

public class NetworkScreen extends Screen {

    //The texture location for the background texture.
    private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(Neuralm.MODID,
                                                                                     "textures/gui/network_background.png");

    //The organism to draw
    private final DrawableOrganism organism;

    //The scale and position at which to render.
    private float scale = 1;
    private float x, y;

    /**
     * Create a new NetworkScreen that will draw the network of the given organism
     *
     * @param organism The organism to draw
     */
    public NetworkScreen(Organism organism) {
        super(new StringTextComponent(organism.getName()));
        this.organism = new DrawableOrganism(organism);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        //Render the background, which is just a tiled texture.
        renderBackground();

        RenderSystem.pushMatrix();
        {
            //Translate to the middle, zoom in, then translate back to the corner.
            //This means it looks like we zoom into the middle instead of the top left corner
            RenderSystem.translated(width / 2f, height / 2f, 0);
            RenderSystem.scaled(scale, scale, 1);
            RenderSystem.translated(-width / 2f, -height / 2f, 0);

            //Translate our based on our x and y so we can move around
            RenderSystem.translated(x, y, 0);

            organism.draw(this);
        }
        RenderSystem.popMatrix();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        //When the player drags the mouse update teh x and y position. This is based on the zoom level to make sure the mouse always stays in the same position relative to the network
        x += dragX / scale;
        y += dragY / scale;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDirection) {
        //When scrolling change the scale.
        double zoomPower = 0.05;
        scale += scrollDirection * zoomPower;

        //Make sure it doesnt go to 0 or negative as that would be bad, also make sure it doesn't get too big
        scale = (float) MathHelper.clamp(scale, 0.1, 3);

        return true;
    }

    @Override
    public void renderBackground() {
        renderBackground(0);
    }

    @Override
    public void renderBackground(int background) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        //Bound the texture so we can draw it
        if (this.minecraft != null) {
            this.minecraft.getTextureManager()
                          .bindTexture(BACKGROUND_LOCATION);
        }

        //Reset color
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //TODO: Fix zooming and possibly switch to using DrawHelper.drawRectangle

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder
                .pos(0.0D, this.height, 0.0D)
                .color(255, 255, 255, 255)
                .tex(0.0F - x / 16, this.height / 16f / scale - y / 16)
                .endVertex();
        bufferbuilder
                .pos(this.width, this.height, 0.0D)
                .color(255, 255, 255, 255)
                .tex((float) this.width / 16f / scale - x / 16, this.height / 16f / scale - y / 16)
                .endVertex();
        bufferbuilder
                .pos(this.width, 0.0D, 0.0D)
                .color(255, 255, 255, 255)
                .tex(this.width / 16f / scale - x / 16, 0 - y / 16)
                .endVertex();
        bufferbuilder
                .pos(0.0D, 0.0D, 0.0D)
                .color(255, 255, 255, 255)
                .tex(0 - x / 16, 0 - y / 16)
                .endVertex();
        tessellator.draw();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
    }

    @Override
    public boolean isPauseScreen() {
        //Don't want the game to pause when this menu is open.
        return false;
    }
}
