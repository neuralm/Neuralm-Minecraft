package net.neuralm.minecraftmod.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.neuralm.minecraftmod.inventory.BotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class BotEntity extends LivingEntity {

    private final BotItemHandler mainInventory = new BotItemHandler(this, BotItemHandler.InventoryType.MAIN);
    private final BotItemHandler armorInventory = new BotItemHandler(this, BotItemHandler.InventoryType.ARMOR);
    private final BotItemHandler offHandInventory = new BotItemHandler(this, BotItemHandler.InventoryType.OFFHAND);

    private FakePlayer fakePlayer;

    private int currentItem = 0;

    private boolean lastTickLeftClicked;

    //Mining related variables
    private float hardness = 0;
    private BlockPos lastMinePos = BlockPos.ZERO;
    private int blockSoundTimer;

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
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

        if(!world.isRemote) {
            //Get all items around the bot and try to pickup those items.
            List<ItemEntity> items = this.world.getEntitiesWithinAABB(ItemEntity.class, this.getBoundingBox(this.getPose()).grow(1.0D, 0.0D, 1.0D));

            for (ItemEntity item : items) {
                pickup(item);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(!world.isRemote) {
            getFakePlayer().tick();
        }

        this.rotationYawHead = this.rotationYaw;
        leftClick(rayTrace());
    }

    /**
     * Simulate the left click action as a player would.
     *
     * @param result The entity or block it's looking at. (or nothing)
     */
    private void leftClick(RayTraceResult result) {
        if (result == null) return;

        if(result instanceof BlockRayTraceResult && result.getType() == RayTraceResult.Type.BLOCK) {
            mine(((BlockRayTraceResult) result).getPos());
        } else {
            resetMining();
        }

        if (result instanceof  EntityRayTraceResult && result.getType() == RayTraceResult.Type.ENTITY) {
            if (!lastTickLeftClicked) {
                if(!world.isRemote && getFakePlayer().getCooledAttackStrength(0)>=1) getFakePlayer().attackTargetEntityWithCurrentItem(((EntityRayTraceResult)result).getEntity());
                swingArm(Hand.MAIN_HAND);

                lastTickLeftClicked = true;
            } else {
                lastTickLeftClicked = false;
            }
        }
    }

    /**
     * Mine the block at the given position.
     * Doesn't do anything when the block is too far away or when {@link World#isRemote} is true.
     * @param pos The position of the block to mine.
     */
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

    /**
     * Reset the mining progress, also resets the mining animation. (cracks on block)
     */
    private void resetMining() {
        hardness = 0;
        this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
        this.lastMinePos.down(255);
    }

    /**
     * Raytrace with the given pitch rotation offsets and the given distance.
     * @param distance The maximum distance
     * @param pitchOffset The pitch offset
     * @param yawOffset The yaw offset
     * @return A raytrace result.
     */
    //TODO: Only hits blocks, doesn't work for entities.
    private RayTraceResult rayTrace(double distance, float pitchOffset, float yawOffset) {
        Vec3d eyePosition = this.getEyePosition(0);

        //Convert rotation from degrees to radians
        float rotationPitchRadians = (this.rotationPitch + pitchOffset) * ((float) Math.PI / 180F);
        float rotationYawRadius = (-this.rotationYaw + -yawOffset) * ((float) Math.PI / 180F);

        float f4 = MathHelper.cos(rotationPitchRadians);
        float xOffset = MathHelper.sin(rotationYawRadius) * f4;
        float yOffset = -MathHelper.sin(rotationPitchRadians);
        float zOffset = MathHelper.cos(rotationYawRadius) * f4;

        Vec3d endPosition = eyePosition.add(xOffset * distance, yOffset * distance, zOffset * distance);
        RayTraceContext ctx = new RayTraceContext(eyePosition, endPosition, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this);

        return this.world.rayTraceBlocks(ctx);
    }

    /**
     * Get the bot's block reach distance.
     * @return The bot's block reach distance
     */
    private float getBlockReachDistance() {
        return (float) this.getAttributes().getAttributeInstanceByName("generic.reachdistance").getValue();

    }

    /**
     * Swing the bot's arm
     * @param hand Which hand to swing
     */
    private void botSwingArm(Hand hand) {
        if (!isSwingInProgress) {
            swingArm(hand);
        }
    }

    /**
     * Update the bot's pose for things like sneaking.
     */
    private void updatePose() {
        Pose pose = Pose.STANDING;

        if (this.isSneaking()) {
            pose = Pose.SNEAKING;
        }

        this.setPose(pose);
    }

    @Override
    @Nonnull
    public Iterable<ItemStack> getArmorInventoryList() {
        return this.armorInventory.getItemStacks();
    }

    @Override
    @Nonnull
    public ItemStack getItemStackFromSlot(@Nonnull EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.mainInventory.getStackInSlot(currentItem);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.offHandInventory.getStackInSlot(0);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            return this.armorInventory.getStackInSlot(slotIn.getIndex());
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if(!world.isRemote){
            getFakePlayer().setItemStackToSlot(slotIn, stack);
        }

        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.mainInventory.setStackInSlot(this.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.playEquipSound(stack);
            this.offHandInventory.setStackInSlot(0, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            this.playEquipSound(stack);
            this.armorInventory.setStackInSlot(slotIn.getIndex(), stack);
        }
    }

    @Override
    @Nonnull
    public HandSide getPrimaryHand() {
        return HandSide.RIGHT;
    }

    /**
     * Try and insert the given item entity into the bot's inventory.
     * @param item The item entity that should be picked up.
     */
    private void pickup(ItemEntity item) {
        if (item.cannotPickup()) return;

        ItemStack stack = item.getItem();

        for (int i = 0; i < this.mainInventory.getSlots() && !stack.isEmpty(); i++) {
            stack = this.mainInventory.insertItem(i, stack, false);
        }

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
            for (int i = 0; i < this.mainInventory.getSlots(); i++) {
                ItemStack itemStack = this.mainInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
                    this.mainInventory.extractItem(i, itemStack.getCount(), true);
                }
            }

            for (int i = 0; i < this.armorInventory.getSlots(); i++) {
                ItemStack itemStack = this.armorInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
                    this.armorInventory.extractItem(i, itemStack.getCount(), true);
                }
            }

            for (int i = 0; i < this.offHandInventory.getSlots(); i++) {
                ItemStack itemStack = this.offHandInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
                    this.offHandInventory.extractItem(i, itemStack.getCount(), true);
                }
            }

            //Send a chat message upon death
            if (world.getServer() != null) {
                world.getServer().getPlayerList().sendMessage(cause.getDeathMessage(this));
            }
        }


    }

    /**
     * Get the fake player associated with this bot.
     * Creates the fake player if it doesn't exist.
     * Always returns null when {@link World#isRemote} is true.
     * @return The fake player
     */
    public FakePlayer getFakePlayer() {
        if (!world.isRemote) {
            if (this.fakePlayer == null) {
                this.fakePlayer = new FakePlayer(this, (ServerWorld) world);
            }

            return this.fakePlayer;

        } else {
            return null;
        }
    }

    /**
     * Ray trace for entities and blocks
     * @return A {@link RayTraceResult} with what was hit.
     */
    public RayTraceResult rayTrace() {
        double reachDistance = (double)this.getBlockReachDistance();
        RayTraceResult objectMouseOver = this.rayTrace(reachDistance, 0, 0);
        Vec3d eyePosition = this.getEyePosition(0);
        boolean flag = false;

        if (reachDistance > 3.0D) {
            flag = true;
        }

        double reachDistanceSqr = reachDistance * reachDistance;

        if (objectMouseOver.getType() != RayTraceResult.Type.MISS) {
            reachDistanceSqr = objectMouseOver.getHitVec().squareDistanceTo(eyePosition);
        }

        //TODO: add pitch and yaw offsets.
        Vec3d raytraceStart = this.getLook(0);
        Vec3d raytraceEnd = eyePosition.add(raytraceStart.x * reachDistance, raytraceStart.y * reachDistance, raytraceStart.z * reachDistance);

        AxisAlignedBB seeDistanceBox = this.getBoundingBox().expand(raytraceStart.scale(reachDistance)).grow(1.0D, 1.0D, 1.0D);

        //Ray trace for entities.
        EntityRayTraceResult rayTraceResult = ProjectileHelper.func_221269_a(world,this, eyePosition, raytraceEnd, seeDistanceBox, (entity) -> !entity.isSpectator() && entity.canBeCollidedWith(), reachDistanceSqr);

        if (rayTraceResult != null) {
            Vec3d hitVec = rayTraceResult.getHitVec();

            double distanceSqr = eyePosition.squareDistanceTo(hitVec);
            if (flag && distanceSqr > 9.0D) {
                objectMouseOver = BlockRayTraceResult.createMiss(hitVec, Direction.getFacingFromVector(raytraceStart.x, raytraceStart.y, raytraceStart.z), new BlockPos(hitVec));
            } else if (distanceSqr < reachDistanceSqr || objectMouseOver.getType() == RayTraceResult.Type.MISS) {
                objectMouseOver = rayTraceResult;
            }
        }

        return objectMouseOver;
    }


}
