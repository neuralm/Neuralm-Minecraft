package net.neuralm.minecraftmod.init;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;
import net.neuralm.minecraftmod.blocks.BotManagerBlock;

@Mod.EventBusSubscriber(modid = Neuralm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Neuralm.MODID)
public class ModBlocks {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        //In here you pass in all item instances you want to register.
        //Make sure you always set the registry name.
        event.getRegistry().registerAll(

                new BotManagerBlock(Block.Properties.create(Material.BARRIER).hardnessAndResistance(-1.0F, 3600000.0F)).setRegistryName(Neuralm.MODID, "botmanager_block")

        );
    }
}
