package net.neuralm.minecraftmod.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BotEntity extends LivingEntity {

    @ObjectHolder(Neuralm.MODID + ":bot")
    public static final EntityType<BotEntity> botEntityType = null;

    FakePlayer fakePlayer;

    final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    final NonNullList<ItemStack> inventory = NonNullList.withSize(9 * 4 + 1, ItemStack.EMPTY);

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);

    }

    @Override
    public void tick() {
        super.tick();
        if(this.firstUpdate) {
            if (!world.isRemote) {
                this.fakePlayer = new FakePlayer(this, (ServerWorld) world);
            } else {
                this.fakePlayer = null;
            }
        }
    }

    @Override
    @NonNull
    public Iterable<ItemStack> getArmorInventoryList() {
        return armor;
    }

    @Override
    @NonNull
    public ItemStack getItemStackFromSlot(@NonNull EquipmentSlotType slotIn) {
        switch (slotIn) {
            case MAINHAND:
            case OFFHAND:
                return inventory.get(slotIn.getIndex()); //TODO: Make the bot be able to choose what it is holding instead of just the first and second item.
            case FEET:
            case LEGS:
            case CHEST:
            case HEAD:
                return armor.get(slotIn.getIndex());
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        switch (slotIn) {
            case MAINHAND:
            case OFFHAND:
                inventory.set(slotIn.getIndex(),stack); //TODO: Make the bot be able to choose what it is holding instead of just the first and second item.
                break;
            case FEET:
            case LEGS:
            case CHEST:
            case HEAD:
                armor.set(slotIn.getIndex(), stack);
                break;
        }
    }

    @Override
    public HandSide getPrimaryHand() {
        return HandSide.RIGHT;
    }
}
