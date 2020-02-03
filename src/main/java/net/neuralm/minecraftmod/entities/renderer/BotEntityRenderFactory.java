package net.neuralm.minecraftmod.entities.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.neuralm.minecraftmod.entities.BotEntity;

public class BotEntityRenderFactory implements IRenderFactory<BotEntity> {

    @Override
    public EntityRenderer<? super BotEntity> createRenderFor(EntityRendererManager manager) {
        return new BotEntityRenderer(manager);
    }

}
