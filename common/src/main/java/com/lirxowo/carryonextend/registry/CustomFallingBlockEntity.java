package com.lirxowo.carryonextend.registry;

import com.lirxowo.carryonextend.platform.Services;
import com.lirxowo.carryonextend.util.FallingBlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CustomFallingBlockEntity extends FallingBlockEntity {

    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<String> BLOCK_ID = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BLOCK_STATE_ID = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.INT);

    private BlockState blockState;
    private int ticksExisted;
    private boolean needsStateUpdate;

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        BlockState safeState = state == null ? Blocks.STONE.defaultBlockState() : state;
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(Services.PLATFORM.getCustomFallingBlockType(), level);
        entity.setPos(x, y, z);
        entity.setBlockState(safeState);

        if (blockData != null) {
            entity.setBlockData(blockData);
        }

        entity.setDeltaMovement(motion);
        entity.time = 1;
        entity.dropItem = true;
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BLOCK_DATA, new CompoundTag());
        entityData.define(BLOCK_ID, "minecraft:stone");
        entityData.define(BLOCK_STATE_ID, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (key.equals(BLOCK_ID) || key.equals(BLOCK_STATE_ID)) {
            needsStateUpdate = true;
        }
    }

    public void setBlockState(BlockState state) {
        blockState = state == null ? Blocks.STONE.defaultBlockState() : state;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        entityData.set(BLOCK_ID, blockId.toString());
        entityData.set(BLOCK_STATE_ID, Block.getId(blockState));
    }

    public void setBlockData(CompoundTag blockData) {
        entityData.set(BLOCK_DATA, blockData);
    }

    public CompoundTag getBlockData() {
        return entityData.get(BLOCK_DATA);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        FallingBlockUtil.saveBlockDataToNbt(tag, getBlockState(), getBlockData(), entityData.get(BLOCK_ID), entityData.get(BLOCK_STATE_ID));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("CustomBlockData")) {
            setBlockData(tag.getCompound("CustomBlockData"));
        }

        blockState = FallingBlockUtil.getBlockStateFromNbt(tag);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        entityData.set(BLOCK_ID, blockId.toString());
        entityData.set(BLOCK_STATE_ID, Block.getId(blockState));
        needsStateUpdate = false;
    }

    @Override
    public BlockState getBlockState() {
        updateBlockStateFromData();
        return blockState == null ? Blocks.STONE.defaultBlockState() : blockState;
    }

    @Override
    public void tick() {
        ticksExisted++;

        if (ticksExisted <= 5) {
            updateBlockStateFromData();
            if (blockState != null && blockState.getBlock() == Blocks.STONE) {
                needsStateUpdate = true;
            }
        }

        if (blockState == null) {
            updateBlockStateFromData();
            if (blockState == null) {
                discard();
                return;
            }
        }

        updateMovement();

        if (!level().isClientSide && !isRemoved() && onGround()) {
            BlockPos pos = blockPosition();
            if (level().getBlockState(pos).canBeReplaced()) {
                placeBlock(pos);
            } else {
                dropAsItem();
            }
        }
    }

    private void updateBlockStateFromData() {
        if (!needsStateUpdate && blockState != null && blockState.getBlock() != Blocks.STONE) {
            return;
        }

        int stateId = entityData.get(BLOCK_STATE_ID);
        if (stateId > 0) {
            BlockState stateFromId = Block.stateById(stateId);
            if (stateFromId.getBlock() != Blocks.AIR) {
                blockState = stateFromId;
                needsStateUpdate = false;
                return;
            }
        }

        ResourceLocation blockId = ResourceLocation.tryParse(entityData.get(BLOCK_ID));
        if (blockId != null) {
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            if (block != Blocks.AIR) {
                blockState = block.defaultBlockState();
                needsStateUpdate = false;
                return;
            }
        }

        blockState = Blocks.STONE.defaultBlockState();
        needsStateUpdate = false;
    }

    private void updateMovement() {
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98D));
    }

    private void placeBlock(BlockPos pos) {
        if (FallingBlockUtil.placeBlock(level(), pos, blockState, getBlockData())) {
            discard();
        }
    }

    private void dropAsItem() {
        if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = FallingBlockUtil.createItemStackWithData(blockState, getBlockData(), blockPosition());
            spawnAtLocation(itemStack);
        }

        discard();
    }
}
