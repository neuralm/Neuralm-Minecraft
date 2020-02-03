package net.neuralm.minecraftmod.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

/**
 * A fakeplayer which can be used when a method is called that needs a player as argument.
 * Most methods in here are just passed through the owner {@link BotEntity}.
 */
public class FakePlayer extends net.minecraftforge.common.util.FakePlayer {

    //The fake player's owner
    private final BotEntity owner;

    /**
     * Create a new fake player for a bot
     * @param owner The {@link BotEntity} that needs this fake player.
     * @param world The world the bot lives in.
     */
    FakePlayer(BotEntity owner, ServerWorld world) {
        super(world, new GameProfile(owner.getUniqueID(), "BOT"));
        this.owner = owner;
    }

    @Override
    @Nonnull
    public BlockPos getPosition() {
        return owner.getPosition();
    }

//    TODO: Find replacement
//    @Override
//    @Nonnull
//    public Vec3d getEyePosition(float partialTicks) {
//        return owner.getEyePosition(partialTicks);
//    }

    @Override
    public void setPosition(double x, double y, double z) {
        if (owner == null) {
            return;
        }

        owner.setPosition(x, y, z);
    }

    @Override
    @Nonnull
    public Vec3d getPositionVec() {
        return owner.getPositionVec();
    }

    @Override
    public Vec3d getPositionVector() {
        return owner.getPositionVector();
    }

    /**
     * Apply the item attributes, this is things like mining speed, damage, health, etc.
     */
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
