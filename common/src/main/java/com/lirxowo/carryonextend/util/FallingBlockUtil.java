package com.lirxowo.carryonextend.util;

import com.lirxowo.carryonextend.platform.Services;
import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.Objects;

public class FallingBlockUtil {

    public static boolean isCustomFallingBlock(CompoundTag entityNbt) {
        if (entityNbt == null || !entityNbt.contains("id")) {
            return false;
        }

        ResourceLocation entityTypeId = ResourceLocation.tryParse(entityNbt.getString("id"));
        EntityType<?> customFallingBlockType = Services.PLATFORM.getCustomFallingBlockType();
        ResourceLocation customTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(customFallingBlockType);
        return customTypeId.equals(entityTypeId);
    }

    public static BlockState getBlockStateFromNbt(CompoundTag entityNbt) {
        if (entityNbt == null) {
            return Blocks.STONE.defaultBlockState();
        }

        if (entityNbt.contains("BlockState")) {
            CompoundTag blockStateTag = entityNbt.getCompound("BlockState");
            int blockStateId = blockStateTag.getInt("id");
            if (blockStateId > 0) {
                BlockState blockState = Block.stateById(blockStateId);
                if (blockState.getBlock() != Blocks.AIR) {
                    return blockState;
                }
            }
        }

        if (entityNbt.contains("BlockId")) {
            ResourceLocation blockId = ResourceLocation.tryParse(entityNbt.getString("BlockId"));
            if (blockId != null) {
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                if (block != Blocks.AIR) {
                    return block.defaultBlockState();
                }
            }
        }

        if (entityNbt.contains("EntityData")) {
            CompoundTag entityData = entityNbt.getCompound("EntityData");
            if (entityData.contains("BLOCK_STATE_META")) {
                int stateId = entityData.getInt("BLOCK_STATE_META");
                if (stateId > 0) {
                    BlockState blockState = Block.stateById(stateId);
                    if (blockState.getBlock() != Blocks.AIR) {
                        return blockState;
                    }
                }
            }

            if (entityData.contains("BLOCK_ID")) {
                ResourceLocation blockId = ResourceLocation.tryParse(entityData.getString("BLOCK_ID"));
                if (blockId != null) {
                    Block block = BuiltInRegistries.BLOCK.get(blockId);
                    if (block != Blocks.AIR) {
                        return block.defaultBlockState();
                    }
                }
            }
        }

        return Blocks.STONE.defaultBlockState();
    }

    public static CompoundTag getBlockData(CompoundTag entityNbt) {
        if (entityNbt == null || !entityNbt.contains("CustomBlockData")) {
            return null;
        }

        return entityNbt.getCompound("CustomBlockData");
    }

    public static void saveBlockDataToNbt(CompoundTag tag, BlockState blockState, CompoundTag blockData, String blockIdValue, int blockStateId) {
        if (blockData != null) {
            tag.put("CustomBlockData", blockData);
        }

        tag.putString("CustomEntityType", "falling_block");

        if (blockState != null) {
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putInt("id", Block.getId(blockState));
            tag.put("BlockState", blockStateTag);
            tag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
        }

        CompoundTag entityData = new CompoundTag();
        entityData.putString("BLOCK_ID", blockIdValue);
        entityData.putInt("BLOCK_STATE_META", blockStateId);
        tag.put("EntityData", entityData);
    }

    public static boolean handleFallingBlockThrow(ServerPlayer player, CarryOnData carry, CompoundTag entityNbt, float throwPower, float throwUpward, float powerFactor) {
        if (!isCustomFallingBlock(entityNbt)) {
            return false;
        }

        BlockState blockState = getBlockStateFromNbt(entityNbt);
        CompoundTag blockData = getBlockData(entityNbt);

        if (blockState.getBlock() == Blocks.AIR) {
            return false;
        }

        Level level = player.level();
        Vec3 playerPos = player.position().add(0, 1.5, 0);
        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);
        CustomFallingBlockEntity.throwBlock(level, playerPos.x, playerPos.y, playerPos.z, blockState, blockData, motion);

        playThrowSound(level, player, powerFactor);
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }

    public static boolean hasValidBlockEntity(Level level, BlockPos pos, CompoundTag blockData) {
        return level.getBlockEntity(pos) != null && blockData != null && !blockData.isEmpty();
    }

    public static boolean placeBlock(Level level, BlockPos pos, BlockState blockState, CompoundTag blockData) {
        if (!level.setBlock(pos, blockState, 3)) {
            return false;
        }

        if (hasValidBlockEntity(level, pos, blockData)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.load(blockData);
                level.sendBlockUpdated(pos, blockState, blockState, 3);
                blockEntity.setChanged();
            }
        }

        return true;
    }

    public static ItemStack createItemStackWithData(BlockState blockState, CompoundTag blockData, BlockPos pos) {
        ItemStack itemStack = new ItemStack(blockState.getBlock());

        if (blockData == null || blockData.isEmpty() || !(blockState.getBlock() instanceof EntityBlock entityBlock)) {
            return itemStack;
        }

        BlockEntity blockEntity = entityBlock.newBlockEntity(pos, blockState);
        if (blockEntity == null) {
            return itemStack;
        }

        blockEntity.load(blockData);
        CompoundTag fullEntityTag = blockEntity.saveWithFullMetadata();
        CompoundTag itemTag = new CompoundTag();

        if (fullEntityTag.contains("ForgeCaps")) {
            itemTag.put("ForgeCaps", fullEntityTag.getCompound("ForgeCaps"));
        }

        CompoundTag blockEntityTag = new CompoundTag();
        for (String key : fullEntityTag.getAllKeys()) {
            if (!key.equals("x") && !key.equals("y") && !key.equals("z")) {
                Tag value = fullEntityTag.get(key);
                blockEntityTag.put(key, Objects.requireNonNull(value));
            }
        }

        itemTag.put("BlockEntityTag", blockEntityTag);
        itemStack.setTag(itemTag);
        return itemStack;
    }

    public static void playThrowSound(Level level, ServerPlayer player, float powerFactor) {
        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
    }
}
