package net.neuralm.minecraftmod.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

@SuppressWarnings("EntityConstructor")
public class FakePlayer extends net.minecraftforge.common.util.FakePlayer {

    final BotEntity owner;

    public FakePlayer(BotEntity owner, ServerWorld world) {
        super(world, new GameProfile(owner.getUniqueID(), "BOT"));
        this.owner = owner;
    }

    @Override
    public BlockPos getPosition() {
        return owner.getPosition();
    }

    @Override
    public Vec3d getEyePosition(float partialTicks) {
        return owner.getEyePosition(partialTicks);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        if (owner == null) {
            return;
        }
        owner.setPosition(x, y, z);
    }

    @Override
    public Vec3d getPositionVec() {
        return owner.getPositionVec();
    }

    @Override
    public Vec3d getPositionVector() {
        return owner.getPositionVector();
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);
    }

    private void applyAttributes() {
        for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
            ItemStack itemstack;
            switch (equipmentslottype.getSlotType()) {
                case HAND:
                case ARMOR:
                    itemstack = this.getItemStackFromSlot(equipmentslottype);
                    break;
                default:
                    continue;
            }

            if (!itemstack.isEmpty()) {
                this.getAttributes().applyAttributeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
            }
        }
    }

    @Override
    public void tick() {
        ++this.ticksSinceLastSwing;
        applyAttributes();
    }
}
