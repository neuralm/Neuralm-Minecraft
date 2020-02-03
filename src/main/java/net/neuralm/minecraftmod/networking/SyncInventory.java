package net.neuralm.minecraftmod.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.neuralm.minecraftmod.entities.BotEntity;

import java.util.function.Supplier;

public class SyncInventory {

    int slot;
    ItemStack item;
    int entityID;
    int selectedIndex;

    public SyncInventory(int slot, int selectedIndex, ItemStack readItemStack, int entityID) {
        this.slot = slot;
        this.item = readItemStack;
        this.entityID = entityID;
        this.selectedIndex = selectedIndex;
    }

    public static void encode(SyncInventory syncInventory, PacketBuffer packetBuffer) {
        packetBuffer.writeInt(syncInventory.slot);
        packetBuffer.writeInt(syncInventory.selectedIndex);
        packetBuffer.writeItemStack(syncInventory.item);
        packetBuffer.writeInt(syncInventory.entityID);
    }


    public static SyncInventory decode(PacketBuffer packetBuffer) {
        return new SyncInventory(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readItemStack(), packetBuffer.readInt());
    }

    public static boolean handle(SyncInventory syncInventory, Supplier<NetworkEvent.Context> contextSupplier) {

        Entity e = Minecraft.getInstance().world.getEntityByID(syncInventory.entityID);

        if(e instanceof BotEntity) {
            BotEntity bot = (BotEntity) e;
            bot.setMainInventory(syncInventory.slot, syncInventory.item, syncInventory.selectedIndex);
        }

        return true;
    }
}
