package net.neuralm.minecraftmod.entities.renderer;

import static net.neuralm.minecraftmod.Neuralm.MODID;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import net.neuralm.minecraftmod.entities.BotEntity;

public class BotEntityRenderer extends LivingRenderer<BotEntity, PlayerModel<BotEntity>> {

    public BotEntityRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new PlayerModel<>(0, false), 0.5f);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(BotEntity entity) {
        return new ResourceLocation(MODID, "textures/entity/bot.png");
    }
}
