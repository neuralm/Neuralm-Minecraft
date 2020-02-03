package net.neuralm.minecraftmod;

import com.google.gson.*;
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
import net.neuralm.client.neat.ConnectionGene;
import net.neuralm.client.neat.Organism;
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

import java.util.Arrays;
import java.util.UUID;

@Mod(Neuralm.MODID)
public class Neuralm {

    public static final String MODID = "neuralm";

    @SuppressWarnings("WeakerAccess")
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    @SuppressWarnings("WeakerAccess")
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    @SuppressWarnings("WeakerAccess")
    public static final DeferredRegister<EntityType<?>> ENTITY = new DeferredRegister<>(ForgeRegistries.ENTITIES,
                                                                                        MODID);

    @SuppressWarnings("WeakerAccess")
    public static final RegistryObject<Block> BOT_MANAGER_BLOCK = BLOCKS.register("botmanager_block",
                                                                                  () -> new BotManagerBlock(
                                                                                          Block.Properties.create(
                                                                                                  Material.BARRIER)
                                                                                                          .hardnessAndResistance(
                                                                                                                  -1.0F,
                                                                                                                  3600000.0F)));

    @SuppressWarnings("unused")
    public static final RegistryObject<Item> BOT_MANAGER_ITEM = ITEMS.register("botmanager_block", () -> new BlockItem(
            BOT_MANAGER_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("unused")
    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item", () -> new TestItem(
            new Item.Properties().maxStackSize(1)));

    public static final Organism TEST_ORGANISM = new Organism(
            Arrays.asList(

                    new ConnectionGene(0, 3, 1, true, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0])),
                    new ConnectionGene(1, 3, 1, false, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0])),
                    new ConnectionGene(2, 3, 1, true, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0])),
                    new ConnectionGene(1, 4, 1, true, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0])),
                    new ConnectionGene(4, 3, 1, true, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0])),
                    new ConnectionGene(0, 4, 1, true, 0, UUID.randomUUID(),
                                       UUID.nameUUIDFromBytes(new byte[0]))

            ),
            3,
            1,
            UUID.nameUUIDFromBytes(new byte[0]),
            0,
            "TEST_ORGANISM",
            0
    );
    public static final RegistryObject<EntityType<BotEntity>> BOT_ENTITY_TYPE = ENTITY.register("bot",
                                                                                                () -> EntityType.Builder.<BotEntity>create(
                                                                                                        BotEntity::new,
                                                                                                        EntityClassification.MISC).build(
                                                                                                        MODID + ":bot"));
    public static Neuralm instance;

    static {
        JsonParser parser = new JsonParser();
        String json = "{\"Organisms\":[{\"Id\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"ConnectionGenes\":[{\"Id\":\"ac3e98a4-e3c3-4aa5-cfbe-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":1,\"OutNodeIdentifier\":2,\"Weight\":0.06650252037984439,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":0,\"OutNodeIdentifier\":3,\"Weight\":-0.3942642088021451,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":3,\"OutNodeIdentifier\":4,\"Weight\":-0.3942642088021451,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":4,\"OutNodeIdentifier\":2,\"Weight\":-0.3942642088021451,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":1,\"OutNodeIdentifier\":4,\"Weight\":-0.3942642088021451,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":0,\"OutNodeIdentifier\":4,\"Weight\":-0.3942642088021451,\"Enabled\":true},{\"Id\":\"32ddaf56-f66d-4a7b-cfbf-08d7a63964eb\",\"OrganismId\":\"b010e455-ae17-46d4-94b4-054696f675dc\",\"InNodeIdentifier\":0,\"OutNodeIdentifier\":2,\"Weight\":-0.3942642088021451,\"Enabled\":true}],\"InputNodes\":[{\"Id\":\"7df0a02f-acea-469c-af0e-2bb8455f9163\",\"Layer\":0,\"NodeIdentifier\":1},{\"Id\":\"d6167a63-8d7a-4d2e-9ea3-8d9d7426882a\",\"Layer\":0,\"NodeIdentifier\":0}],\"OutputNodes\":[{\"Id\":\"505e0674-98b1-44c2-aabc-4ebd9b57ba89\",\"Layer\":0,\"NodeIdentifier\":2}],\"Score\":0,\"Name\":\"gaaj\",\"Generation\":1}],\"Id\":\"2e29f32a-6bfb-4ca9-b7c9-643dc8e622eb\",\"RequestId\":\"a603e8b7-703a-4c57-9877-9d71c66de612\",\"DateTime\":\"2020-01-31T10:36:28.3029641Z\",\"Message\":\"Start of new generation.\",\"Success\":true}";
        JsonObject object = parser.parse(json)
                                  .getAsJsonObject();
        JsonArray organisms = object.getAsJsonArray("Organisms");

        Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                                     .create();
        Organism organism = gson.fromJson(organisms.get(0), Organism.class);
        organism.initialize();

//        TEST_ORGANISM = organism;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger;
    public NeuralmClient client;

    public Neuralm() {
        instance = this;
        logger = LogManager.getLogger();

        //Register the event handlers to forge.
        BLOCKS.register(FMLJavaModLoadingContext.get()
                                                .getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get()
                                               .getModEventBus());
        ENTITY.register(FMLJavaModLoadingContext.get()
                                                .getModEventBus());
        FMLJavaModLoadingContext.get()
                                .getModEventBus()
                                .addListener(this::registerEntityRender);
        FMLJavaModLoadingContext.get()
                                .getModEventBus()
                                .addListener(this::commonSetupEventHandler);

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
