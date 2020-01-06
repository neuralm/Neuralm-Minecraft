package net.neuralm.minecraftmod;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.client.NeuralmClient;
import net.neuralm.minecraftmod.commands.ConnectCommand;
import net.neuralm.minecraftmod.commands.LoginCommand;
import net.neuralm.minecraftmod.commands.RegisterCommand;
import net.neuralm.minecraftmod.entities.BotEntity;
import net.neuralm.minecraftmod.entities.renderer.BotEntityRenderFactory;
import net.neuralm.minecraftmod.networking.PacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Neuralm.MODID)
public class Neuralm {

    public static final String MODID = "neuralm";

    @ObjectHolder(MODID+":bot")
    public static final EntityType<BotEntity> BOT_ENTITY_TYPE = null;

    public static Neuralm instance;
    public NeuralmClient client;
    private final Logger logger;

    public Neuralm() {
        instance = this;
        logger = LogManager.getLogger();

        //Register the event handlers to forge.
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntityType);
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
        RenderingRegistry.registerEntityRenderingHandler(BOT_ENTITY_TYPE, new BotEntityRenderFactory());
    }

    /***
     * Register the entitytype so forge and minecraft know the entity exists.
     * @param event The register event
     */
    private void registerEntityType(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(

            EntityType.Builder.create(BotEntity::new, EntityClassification.MISC).build(MODID + ":bot").setRegistryName(MODID, "bot")

        );
    }

    /***
     * Register the test item, it has no use for the final project but it is nice to have an item you can use to activate stuff on demand.
     * @param event The register Item event
     */
    private void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(

            new Item(new Properties().maxStackSize(1)) {

                @Override
                public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

                    //If you need it to do anything on right click add your code here

                    return super.onItemRightClick(worldIn, playerIn, handIn);
                }

            }.setRegistryName(MODID, "test_item")

        );
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
