package net.neuralm.minecraftmod.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BotEntity extends LivingEntity {

    @ObjectHolder(Neuralm.MODID + ":bot")
    public static final EntityType<BotEntity> botEntityType = null;

    private FakePlayer fakePlayer;

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);

    }

    @Override
    @NonNull
    public Iterable<ItemStack> getArmorInventoryList() {
        return this.getFakePlayer().getArmorInventoryList();
    }

    @Override
    @NonNull
    public ItemStack getItemStackFromSlot(@NonNull EquipmentSlotType slotIn) {
        return this.getFakePlayer().getItemStackFromSlot(slotIn);
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        getFakePlayer().setItemStackToSlot(slotIn, stack);
    }

    @Override
    public HandSide getPrimaryHand() {
        return getFakePlayer().getPrimaryHand();
    }

    private FakePlayer getFakePlayer() {
        if (!world.isRemote) {
            if (this.fakePlayer == null) {
                this.fakePlayer = new FakePlayer(this, (ServerWorld) world);
            }

            return this.fakePlayer;

        } else {
            return null;
        }
    }


}
