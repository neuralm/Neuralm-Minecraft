package net.neuralm.minecraftmod.entities.renderer;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import net.neuralm.minecraftmod.entities.BotEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.neuralm.minecraftmod.Neuralm.MODID;

public class BotEntityRenderer extends LivingRenderer<BotEntity, BipedModel<BotEntity>> {

    public BotEntityRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new PlayerModel<>(0, false), 0.5f);
    }


    @Nullable
    @Override
    public ResourceLocation getEntityTexture(@Nonnull BotEntity entity) {
        return new ResourceLocation(MODID, "textures/entity/bot.png");
    }
}
