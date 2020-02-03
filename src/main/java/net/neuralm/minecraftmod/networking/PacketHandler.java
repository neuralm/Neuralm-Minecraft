package net.neuralm.minecraftmod.networking;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.neuralm.minecraftmod.Neuralm;

import java.util.Objects;

public class PacketHandler {
    public static SimpleChannel channel;
    private static ResourceLocation networkName = new ResourceLocation(Neuralm.MODID, "net");

    public static void registerChannel() {
        channel = NetworkRegistry.ChannelBuilder.named(networkName).
                //Update version number when updated
                        clientAcceptedVersions(s -> Objects.equals(s, "1")).
                        serverAcceptedVersions(s -> Objects.equals(s, "1")).
                        networkProtocolVersion(() -> "1").
                        simpleChannel();

        channel.messageBuilder(SyncInventory.class, 0)
                .encoder(SyncInventory::encode)
                .decoder(SyncInventory::decode)
                .consumer(SyncInventory::handle)
                .add();
    }
}
