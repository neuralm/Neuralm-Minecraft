package net.neuralm.minecraftmod.entities.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ResourceLocation;
import net.neuralm.minecraftmod.entities.BotEntity;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.Proxy;
import java.util.Objects;
import java.util.UUID;

public class BotEntityRenderer extends LivingRenderer<BotEntity, PlayerModel<BotEntity>> {

    private static PlayerProfileCache playerprofilecache;
    private static MinecraftSessionService service;

    private PlayerModel<BotEntity> modelNormal = new PlayerModel<>(0.0f, false);
    private PlayerModel<BotEntity> modelSlim = new PlayerModel<>(0.0f, true);

    BotEntityRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new PlayerModel<>(0, false), 0.5f);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
        this.addLayer(new BeeStingerLayer<>(this));

        if (service == null || playerprofilecache == null) {
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(
                    Proxy.NO_PROXY, UUID.randomUUID()
                                        .toString());
            service = yggdrasilauthenticationservice.createMinecraftSessionService();

            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(Minecraft.getInstance().gameDir,
                                                                                        MinecraftServer.USER_CACHE_FILE.getName()));
        }
    }

    @Override
    public void render(BotEntity bot, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStackIn,
                       @Nonnull IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (bot.playerTexturesLoaded && bot.skinType != null && bot.skinType.equals("slim")) {
            this.entityModel = modelSlim;
        } else {
            this.entityModel = modelNormal;
        }
        super.render(bot, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    @Nonnull
    public ResourceLocation getEntityTexture(@Nonnull BotEntity entity) {
        if (!entity.playerTexturesLoaded && !entity.isTextureLoading) {
            loadPlayerTextures(entity);
        }

        return entity.playerTexturesLoaded ? entity.playerTextures.get(
                MinecraftProfileTexture.Type.SKIN) : DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    private void loadPlayerTextures(BotEntity bot) {

        if (!bot.isTextureLoading) {
            bot.isTextureLoading = true;
            new Thread(new SkinLoader(bot.getName()
                                         .getFormattedText(), bot.getUniqueID(), bot.getEntityId(), playerprofilecache,
                                      service), "SkinLoader" + bot.getName()).start();
        }
    }
}

class SkinLoader implements Runnable {
    private PlayerProfileCache playerprofilecache;
    private MinecraftSessionService service;

    private String name;
    private UUID uuid;
    private int entityID;

    SkinLoader(String name, UUID uniqueID, int id, PlayerProfileCache playerProfileCache,
               MinecraftSessionService service) {
        this.name = name;
        this.uuid = uniqueID;
        this.playerprofilecache = playerProfileCache;
        this.service = service;
        this.entityID = id;
    }

    private synchronized static GameProfile getGameProfileForUsername(PlayerProfileCache playerprofilecache,
                                                                      String name) {
        return playerprofilecache.getGameProfileForUsername(name);
    }

    @Override
    public void run() {
        GameProfile profile = getGameProfileForUsername(playerprofilecache, name);

        if (profile == null) {
            profile = new GameProfile(uuid, name);
        }

        if (!profile.getProperties()
                    .containsKey("textures")) {
            service.fillProfileProperties(profile, true);
        }

        GameProfile finalProfile = profile;
        Minecraft.getInstance()
                 .enqueue(() -> {
                     Entity e = Objects.requireNonNull(Minecraft.getInstance().world)
                                       .getEntityByID(entityID);
                     if (e instanceof BotEntity) {
                         BotEntity bot = (BotEntity) e;

                         Minecraft.getInstance()
                                  .getSkinManager()
                                  .loadProfileTextures(finalProfile, (typeIn, location, profileTexture) -> {
                                      switch (typeIn) {
                                          case SKIN:
                                              bot.playerTextures.put(MinecraftProfileTexture.Type.SKIN, location);
                                              bot.skinType = profileTexture.getMetadata("model");

                                              if (bot.skinType == null) {
                                                  bot.skinType = "default";
                                              }

                                              break;
                                          case CAPE:
                                              bot.playerTextures.put(MinecraftProfileTexture.Type.CAPE, location);
                                              break;
                                          case ELYTRA:
                                              bot.playerTextures.put(MinecraftProfileTexture.Type.ELYTRA, location);
                                      }

                                      bot.playerTexturesLoaded = true;
                                      bot.isTextureLoading = false;
                                  }, true);
                     }
                 });
    }
}