package net.neuralm.minecraftmod.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.neuralm.minecraftmod.Neuralm;
import net.neuralm.minecraftmod.inventory.ItemHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class BotEntity extends LivingEntity {
    public final NonNullList<ItemStack> mainInventory = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armorInventory = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offHandInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    public int currentItem;
    private final ItemHandler itemHandler;
    public boolean lastTickLeftClicked;
    public FakePlayer fakePlayer;
    float hardness = 0;
    @ObjectHolder(Neuralm.MODID + ":bot")
    public static final EntityType<BotEntity> botEntityType = null;
    private BlockPos lastMinePos = BlockPos.ZERO;
    private int blockSoundTimer;

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        itemHandler = new ItemHandler(this);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(PlayerEntity.REACH_DISTANCE);
    }

    @Override
    public void livingTick() {
        super.livingTick();
        this.updateArmSwingProgress();
        this.updatePose();
        this.updateEntityActionState();

        List<ItemEntity> items = this.world.getEntitiesWithinAABB(ItemEntity.class, this.getBoundingBox(this.getPose()).grow(1.0D, 0.0D, 1.0D));

        for (ItemEntity item : items) {
            pickup(item);
        }

    }


    @Override
    public void tick() {
        super.tick();

        this.rotationYawHead = this.rotationYaw;
        leftClick(rayTrace(this.getBlockReachDistance(), 0, 0));
    }

    //This is broken and I dont know how to fix it... @SupperGerrie2 look into this please
    private void leftClick(BlockRayTraceResult result) {

        if (result == null) return;

        switch (result.getType()) {
            case BLOCK:

                mine(result.getPos());

                break;
            case ENTITY:
                if (!lastTickLeftClicked) {
//                    fakePlayer.attackTargetEntityWithCurrentItem();
//                    swingArm(Hand.MAIN_HAND);

                }
            case MISS:
            default:
                resetMining();
                break;
        }
        lastTickLeftClicked = true;
    }

    //Allows the bot to mine the block in front of them
    private void mine(BlockPos pos) {
        if (world.isRemote) {
            return;
        }

        if (!this.world.getWorldBorder().contains(pos) || pos.distanceSq(getPosition()) > this.getBlockReachDistance() * this.getBlockReachDistance()) {
            resetMining();
            return;
        }

        if (!lastMinePos.equals(pos)) {
            resetMining();
        }

        lastMinePos = pos;


        BlockState state = world.getBlockState(pos);
        if (this.blockSoundTimer % 4.0F == 0.0F) {
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, this);
            this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0f) / 8.0f, soundtype.getPitch() * 0.5f, false);
        }

        ++this.blockSoundTimer;

        //Get the relative block hardness for the bot
        float f = state.getBlockHardness(world, pos);
        if (f == -1.0F) {
            hardness += 0;
        } else {
            int i = net.minecraftforge.common.ForgeHooks.canHarvestBlock(state, getFakePlayer(), world, pos) ? 30 : 100;
            hardness += (getFakePlayer().getDigSpeed(state, pos)) / f / (float) i;
        }

        //Send the break progress to the client and make the bot swing its arm
        this.world.sendBlockBreakProgress(this.getEntityId(), pos, (int) ((hardness * 10.0F) * 4));
        botSwingArm(Hand.MAIN_HAND);

        //Check if block has been broken
        if (hardness >= (1.0f / 4)) {
            //Block broken

            hardness = 0;
            this.blockSoundTimer = 0;
            world.playEvent(2001, pos, Block.getStateId(state));

            ItemStack itemstack = this.getActiveItemStack();
            if (itemstack.getItem().onBlockStartBreak(itemstack, pos, getFakePlayer())) {
                return;
            }


            boolean harvest = state.getBlock().canHarvestBlock(state, world, pos, getFakePlayer());

            itemstack.onBlockDestroyed(world, state, pos, getFakePlayer());

            state.getBlock().onBlockHarvested(world, pos, state, getFakePlayer());

            if (state.getBlock().removedByPlayer(state, world, pos, getFakePlayer(), true, null)) {
                state.getBlock().onPlayerDestroy(world, pos, state);
            } else {
                harvest = false;
            }

            if (harvest) {
                state.getBlock().harvestBlock(world, getFakePlayer(), pos, state, world.getTileEntity(pos), itemstack);
            }
        }
    }

    //Resets the mining progress
    private void resetMining() {
        hardness = 0;
        this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
        this.lastMinePos.down(255);
    }

    //Gets the raytrace result of the block in front of the bot's vision
    private BlockRayTraceResult rayTrace(double blockReachDistance, float rotatePitch, float rotateYaw) {
        Vec3d vec3d = this.getEyePosition(0);
//        Vec3d vec3d1 = this.getLookVec();
        float f = (this.rotationPitch + rotatePitch) * ((float) Math.PI / 180F);
        float f1 = (-this.rotationYaw + -rotateYaw) * ((float) Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        float xOffset = f3 * f4;
        float yOffset = -f5;
        float zOffset = f2 * f4;

        Vec3d vec3d2 = vec3d.add(xOffset * blockReachDistance, yOffset * blockReachDistance, zOffset * blockReachDistance);
        RayTraceContext ctx = new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this);
        BlockRayTraceResult result = this.world.rayTraceBlocks(ctx);
        return new BlockRayTraceResult(result.getHitVec(), result.getFace(), result.getPos(), false);
    }

    //Gets the reach distance of the bot
    private float getBlockReachDistance() {
        return (float) this.getAttributes().getAttributeInstanceByName("generic.reachdistance").getValue();

    }

    //Swings the bot's arm
    public void botSwingArm(Hand hand) {
        if (!isSwingInProgress) {
            swingArm(hand);
        }
    }

    //Updates the pose for things like sneaking
    public void updatePose() {
        Pose pose;
        if (this.isSneaking()) {
            pose = Pose.SNEAKING;
            this.setPose(pose);
        }

    }

    //Copy paste from player entity so it works. Does nothing right now
    @Override
    @NonNull
    public Iterable<ItemStack> getArmorInventoryList() {

        return this.armorInventory;
    }

    //Copy paste from player entity so it works. Does nothing right now
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

    //Copy paste from player entity so it works. Does nothing right now
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

    //Pickup items around the block
    private void pickup(ItemEntity item) {
        if (item.cannotPickup()) return;

        ItemStack stack = item.getItem();


        for (int i = 0; i < this.itemHandler.getSlots() && !stack.isEmpty(); i++) {
            stack = this.itemHandler.insertItem(i, stack, false);

//            PacketHandler.INSTANCE.sendToAllTracking(new SyncHandsMessage(this.itemHandler.getStackInSlot(i), getEntityId(), i, selectedItemIndex), this);
        }

        this.setHeldItem(Hand.MAIN_HAND, this.itemHandler.getStackInSlot(this.currentItem));

        if (stack.isEmpty()) {
            item.remove();
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        //Reset mining so there isnt a crack in a block forever
        this.resetMining();


        if (!this.world.isRemote()) {

            //Drop all items in the inventory
            for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                ItemStack itemStack = this.itemHandler.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
                    this.itemHandler.extractItem(i, itemStack.getCount(), true);
                }
            }

            //Send a chat message upon death
            if (world.getServer() != null) {
                world.getServer().getPlayerList().sendMessage(cause.getDeathMessage(this));
            }
        }


    }

    //Make sure the fake player is not null
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
