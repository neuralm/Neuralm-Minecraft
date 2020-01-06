package net.neuralm.minecraftmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.neuralm.client.NeuralmClient;
import net.neuralm.minecraftmod.blocks.BotManagerBlock;
import net.neuralm.minecraftmod.commands.ConnectCommand;
import net.neuralm.minecraftmod.commands.LoginCommand;
import net.neuralm.minecraftmod.commands.RegisterCommand;
import net.neuralm.minecraftmod.entities.BotEntity;
import net.neuralm.minecraftmod.entities.renderer.BotEntityRenderFactory;
import net.neuralm.minecraftmod.items.TestItem;
import net.neuralm.minecraftmod.networking.PacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Neuralm.MODID)
public class Neuralm {

    public static final String MODID = "neuralm";

    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS  = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY = new DeferredRegister<>(ForgeRegistries.ENTITIES, MODID);

    public static final RegistryObject<Block> BOT_MANAGER_BLOCK = BLOCKS.register("botmanager_block", () -> new BotManagerBlock(Block.Properties.create(Material.BARRIER).hardnessAndResistance(-1.0F, 3600000.0F)));

    @SuppressWarnings("unused")
    public static final RegistryObject<Item> BOT_MANAGER_ITEM = ITEMS.register("botmanager_block", () -> new BlockItem(BOT_MANAGER_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item", () -> new TestItem(new Item.Properties().maxStackSize(1)));

    public static final RegistryObject<EntityType<BotEntity>> BOT_ENTITY_TYPE = ENTITY.register("bot", () -> EntityType.Builder.create(BotEntity::new, EntityClassification.MISC).build(MODID + ":bot"));

    public static Neuralm instance;
    public NeuralmClient client;
    private final Logger logger;

    public Neuralm() {
        instance = this;
        logger = LogManager.getLogger();

        //Register the event handlers to forge.
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerEntityRender);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetupEventHandler);

        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    /***
     * Do setup stuff that should happen on both the client and server.
     * Ex. setting up the mod's network channel
     * @param event The event fired by forge
     */
    private void commonSetupEventHandler(FMLCommonSetupEvent event) {
        PacketHandler.registerChannel();
    }

    /***
     * Register the entity renderer
     * @param event Event fired when the client is setup
     */
    private void registerEntityRender(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BOT_ENTITY_TYPE.get(), new BotEntityRenderFactory());
    }

    /***
     * In this event the commands are registered
     * @param e The event fired when the server starts
     */
    private void onServerStarting(final FMLServerStartingEvent e) {
        LoginCommand.register(e.getCommandDispatcher());
        ConnectCommand.register(e.getCommandDispatcher());
        RegisterCommand.register(e.getCommandDispatcher());
    }

}
