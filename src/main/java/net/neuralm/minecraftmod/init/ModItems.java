package net.neuralm.minecraftmod.init;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;

@Mod.EventBusSubscriber(modid = Neuralm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Neuralm.MODID)
public class ModItems {


    public static final Block botmanager_block = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        //In here you pass in all item instances you want to register.
        //Make sure you always set the registry name.
        event.getRegistry().registerAll(

                new BlockItem(botmanager_block, new Item.Properties()).setRegistryName(botmanager_block.getRegistryName())
        );
    }
}
