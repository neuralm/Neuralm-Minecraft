package net.neuralm.minecraftmod.inventory;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import net.neuralm.minecraftmod.entities.BotEntity;

public class BotItemHandler extends ItemStackHandler {

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

    final BotEntity owner;

    final InventoryType inventoryType;

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
