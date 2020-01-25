package net.neuralm.minecraftmod.entities;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.neuralm.minecraftmod.inventory.BotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Hashtable;
import java.util.List;

public class BotEntity extends LivingEntity {

    //Bot has 3 separate inventories, the main inventory which you can see as the hotbar and the 9*3 grid
    private final BotItemHandler mainInventory = new BotItemHandler(this, BotItemHandler.InventoryType.MAIN);
    //The armor inventory which are the 4 slots that can contain armor
    private final BotItemHandler armorInventory = new BotItemHandler(this, BotItemHandler.InventoryType.ARMOR);
    //The offhand inventory is just a single lonely slot
    private final BotItemHandler offHandInventory = new BotItemHandler(this, BotItemHandler.InventoryType.OFFHAND);

    //Has this bot loaded its skin yet?
    public boolean playerTexturesLoaded;
    //Is this bot's texture loading?
    public boolean isTextureLoading;
    //The textures this bot has
    public Hashtable<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = new Hashtable<>();
    //The skin's type (slim, or default)
    public String skinType;

    //To interact with the world
    private FakePlayer fakePlayer;

    //The current selected item
    public int selectedItem = 0;

    //Whether the bot has tried left clicking last tick.
    private boolean lastTickLeftClicked;

    //Mining related variables
    private float hardness = 0;
    private BlockPos lastMinePos = BlockPos.ZERO;
    private int blockSoundTimer;

    public BotEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);

        String[] names = new String[] {
                "suppergerrie2", "MechanistPlays", "Glovali"};

        String name = names[world.rand.nextInt(names.length)];

        this.setCustomName(new StringTextComponent(name));
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
    private void leftClick(@Nullable RayTraceResult result) {
        if (result == null) return;

        //If it is seeing a block, is should mine
        if(result instanceof BlockRayTraceResult && result.getType() == RayTraceResult.Type.BLOCK) {
            mine(((BlockRayTraceResult) result).getPos());
        } else {
            //If it doesnt see a block, mining should be reset
            resetMining();
        }

        //If it is seeing an entity it should attack, but only if it didnt left click last tick.
        if (result instanceof  EntityRayTraceResult && result.getType() == RayTraceResult.Type.ENTITY) {
            if (!lastTickLeftClicked) {
                //If we can attack (according to the fake player) we attack. TODO: Should this be here or should it be able to spam attack if it wants?
                if(!world.isRemote && getFakePlayer().getCooledAttackStrength(0)>=1) {
                    getFakePlayer().attackTargetEntityWithCurrentItem(((EntityRayTraceResult)result).getEntity());
                }

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
    //TODO: Its mining speed doesn't match the player, should be fixed
    private void mine(BlockPos pos) {
        if (world.isRemote) {
            return;
        }

        //If the block is outside of the border, or it is too far away from the bot reset mining.
        if (!this.world.getWorldBorder().contains(pos) || pos.distanceSq(getPosition()) > this.getBlockReachDistance() * this.getBlockReachDistance()) {
            resetMining();
            return;
        }

        //If we started mining a new position, reset mining before continuing.
        if (!lastMinePos.equals(pos)) {
            resetMining();
        }

        //Update the position we are mining.
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

//        if (this.isSneaking()) { TODO: Find replacement
//            pose = Pose.SNEAKING;
//        }


        this.setPose(pose);
    }

    @Override
    @Nonnull
    public Iterable<ItemStack> getArmorInventoryList() {
        return this.armorInventory.getItemStacks();
    }

    /**
     * Returns the ItemStack in the given slot
     *
     * This method will check for the slot type so it can get it from the right inventory.
     * @param slotIn The slot to get the item from
     * @return The itemstack in the slot, can be {@link ItemStack#EMPTY} if the slot is empty or non-existent on this bot.
     */
    @Override
    @Nonnull
    public ItemStack getItemStackFromSlot(@Nonnull EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.mainInventory.getStackInSlot(selectedItem);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.offHandInventory.getStackInSlot(0);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            return this.armorInventory.getStackInSlot(slotIn.getIndex());
        }

        return ItemStack.EMPTY;
    }

    /**
     * Sets the given {@link ItemStack} to the slot corresponding to the given {@link EquipmentSlotType}.
     *
     * This method will sync the item to the fake player if on the server side.
     * @param slotIn
     * @param stack
     */
    @Override
    public void setItemStackToSlot(@Nonnull EquipmentSlotType slotIn, @Nonnull ItemStack stack) {
        if(!world.isRemote){
            getFakePlayer().setItemStackToSlot(slotIn, stack);
        }

        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.mainInventory.setStackInSlot(this.selectedItem, stack);
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
     * Try and insert the given {@link ItemEntity} into the bot's inventory.
     * @param item The item entity that should be picked up.
     */
    private void pickup(ItemEntity item) {
        if (item.cannotPickup()) return;

        ItemStack stack = item.getItem();

        //Find the first empty slot and insert the item
        for (int i = 0; i < this.mainInventory.getSlots() && !stack.isEmpty(); i++) {
            stack = this.mainInventory.insertItem(i, stack, false);
        }

        //If all items were inserted the itementity can be removed
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
                }
            }

            for (int i = 0; i < this.armorInventory.getSlots(); i++) {
                ItemStack itemStack = this.armorInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
                }
            }

            for (int i = 0; i < this.offHandInventory.getSlots(); i++) {
                ItemStack itemStack = this.offHandInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.entityDropItem(itemStack);
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

        //Ray tracec for a block first
        double reachDistance = this.getBlockReachDistance();
        RayTraceResult raytraceResult = this.rayTraceBlock(reachDistance, 0, 0);

        Vec3d eyePosition = this.getEyePosition(0);

        double maxDistanceSqr = reachDistance * reachDistance;

        //If there is a block the max distance we check is the distance to that block, if an entity is closer that means we see the entity
        if (raytraceResult.getType() != RayTraceResult.Type.MISS) {
            maxDistanceSqr = raytraceResult.getHitVec().squareDistanceTo(eyePosition);
        }

        //TODO: add pitch and yaw offsets.
        Vec3d raytraceStart = this.getLook(0);
        Vec3d raytraceEnd = eyePosition.add(raytraceStart.x * reachDistance, raytraceStart.y * reachDistance, raytraceStart.z * reachDistance);

        //This is the box entities can be in and visible
        AxisAlignedBB seeDistanceBox = this.getBoundingBox().expand(raytraceStart.scale(reachDistance)).grow(1.0D, 1.0D, 1.0D);

        //Ray trace for entities.
        EntityRayTraceResult rayTraceResult = ProjectileHelper.rayTraceEntities(world,this, eyePosition, raytraceEnd, seeDistanceBox, (entity) -> !entity.isSpectator() && entity.canBeCollidedWith(), maxDistanceSqr);

        if (rayTraceResult != null) {
            Vec3d hitVec = rayTraceResult.getHitVec();

            double distanceSqr = eyePosition.squareDistanceTo(hitVec);
            if (distanceSqr >= maxDistanceSqr) {
                raytraceResult = BlockRayTraceResult.createMiss(hitVec, Direction.getFacingFromVector(raytraceStart.x, raytraceStart.y, raytraceStart.z), new BlockPos(hitVec));
            } else if (distanceSqr < maxDistanceSqr || raytraceResult.getType() == RayTraceResult.Type.MISS) {
                raytraceResult = rayTraceResult;
            }
        }

        return raytraceResult;
    }

    /**
     * Raytrace with the given pitch rotation offsets and the given distance.
     * @param distance The maximum distance
     * @param pitchOffset The pitch offset
     * @param yawOffset The yaw offset
     * @return A raytrace result.
     */
    private RayTraceResult rayTraceBlock(double distance, float pitchOffset, float yawOffset) {
        Vec3d eyePosition = this.getEyePosition(0);

        //Convert rotation from degrees to radians
        float rotationPitchRadians = (this.rotationPitch + pitchOffset) * ((float) Math.PI / 180F);
        float rotationYawRadius = (-this.rotationYaw + -yawOffset) * ((float) Math.PI / 180F);

        float horizontalScale = MathHelper.cos(rotationPitchRadians);
        float xOffset = MathHelper.sin(rotationYawRadius) * horizontalScale;
        float yOffset = -MathHelper.sin(rotationPitchRadians);
        float zOffset = MathHelper.cos(rotationYawRadius) * horizontalScale;

        Vec3d endPosition = eyePosition.add(xOffset * distance, yOffset * distance, zOffset * distance);
        RayTraceContext ctx = new RayTraceContext(eyePosition, endPosition, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this);

        return this.world.rayTraceBlocks(ctx);
    }

    public void setMainInventory(int slot, ItemStack item, int selectedIndex) {
        this.mainInventory.setStackInSlot(slot, item);
        selectedItem = selectedIndex;
    }
}
