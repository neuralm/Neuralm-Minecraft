package net.neuralm.minecraftmod.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BotEntity extends LivingEntity {
    public final NonNullList<ItemStack> mainInventory = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offHandInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    public int currentItem;

    @ObjectHolder(Neuralm.MODID + ":bot")
    public static final EntityType<BotEntity> botEntityType = null;

    private FakePlayer fakePlayer;

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);

    }

    @Override
    public void livingTick() {
        super.livingTick();
        this.updateArmSwingProgress();
        this.updatePose();
        this.updateEntityActionState();

        this.setSneaking(true);

        botSwingArm(Hand.MAIN_HAND);
        botSwingArm(Hand.OFF_HAND);
    }

    public void botSwingArm(Hand hand) {
        if (!isSwingInProgress) {
            swingArm(hand);
        }
    }

    public void updatePose() {
        Pose pose;
        if (this.isSneaking()) {
            pose = Pose.SNEAKING;
            this.setPose(pose);
        }

    }

    @Override
    @NonNull
    public Iterable<ItemStack> getArmorInventoryList() {

        return this.armorInventory;
    }

    @Override
    @NonNull
    public ItemStack getItemStackFromSlot(@NonNull EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.mainInventory.get(currentItem);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.offHandInventory.get(0);
        } else {
            return slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR ? this.armorInventory.get(slotIn.getIndex()) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.mainInventory.set(this.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.playEquipSound(stack);
            this.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            this.playEquipSound(stack);
            this.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

    @Override
    public HandSide getPrimaryHand() {
        return HandSide.RIGHT;
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
