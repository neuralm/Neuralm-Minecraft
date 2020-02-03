package net.neuralm.minecraftmod.inventory;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemStackHandler;
import net.neuralm.minecraftmod.entities.BotEntity;
import net.neuralm.minecraftmod.networking.PacketHandler;
import net.neuralm.minecraftmod.networking.SyncInventory;

public class BotItemHandler extends ItemStackHandler {

    /***
     * Inventory type is used to configure the size of the item handler.
     *
     * {@link InventoryType#MAIN} is the inventory you'd see when you open your inventory. The hotbar is also included
     * {@link InventoryType#ARMOR} is the inventory where your armor is stored, a normal player would see these slots next to the player model in the inventory.
     * {@link InventoryType#OFFHAND} is the inventory where the offhand is stored. This is a different inventory from the main inventory because this slot is always selected
     */
    public enum InventoryType {
        MAIN(9 * 4),
        ARMOR(4),
        OFFHAND(1);

        private final int size;

        InventoryType(int size) {
            this.size = size;
        }

        int getSize() {
            return this.size;
        }
    }

    //The owner of this itemhandler
    private final BotEntity owner;

    //What type this itemhandler is
    private final InventoryType inventoryType;

    /**
     * Create the bot item handler for the given inventory type.
     * @param owner The bot whose inventory this will be.
     * @param inventoryType The type of inventory
     * @see InventoryType
     */
    public BotItemHandler(BotEntity owner, InventoryType inventoryType) {
        super(inventoryType.getSize());
        this.owner = owner;
        this.inventoryType = inventoryType;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (owner.getFakePlayer() == null) {
            return;
        }

        if(!owner.world.isRemote) PacketHandler.channel.send(PacketDistributor.TRACKING_ENTITY.with(()->owner), new SyncInventory(slot, owner.selectedItem, getStackInSlot(slot), owner.getEntityId()));

        //Sync this inventory to the fakeplayer, what slot to use depends on the InventoryType
        switch (this.inventoryType) {
            case MAIN:
                owner.getFakePlayer().inventory.setInventorySlotContents(slot, getStackInSlot(slot));
                break;
            case ARMOR:
                owner.getFakePlayer().setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot), getStackInSlot(slot));
                break;
            case OFFHAND:
                owner.getFakePlayer().setItemStackToSlot(EquipmentSlotType.OFFHAND, getStackInSlot(slot));
                break;
        }
    }

    public NonNullList<ItemStack> getItemStacks() {
        return this.stacks;
    }
}
