package com.lirxowo.carryonextend.client.renderer;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CustomFallingBlockRenderer extends EntityRenderer<CustomFallingBlockEntity> {

    private final BlockRenderDispatcher dispatcher;
    private float partialTicks;

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
        dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CustomFallingBlockEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.partialTicks = partialTicks;
        BlockState blockState = entity.getBlockState();
        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        Level level = entity.level();
        BlockPos renderPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        poseStack.pushPose();

        try {
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            renderBlockModel(blockState, poseStack, buffer, packedLight);
            if (blockState.hasBlockEntity()) {
                renderBlockEntity(entity, blockState, level, renderPos, poseStack, buffer, packedLight);
            }
        } finally {
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBlockModel(BlockState blockState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            dispatcher.renderSingleBlock(blockState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    private void renderBlockEntity(CustomFallingBlockEntity entity, BlockState blockState, Level level, BlockPos renderPos, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockEntityType<?> blockEntityType = findBlockEntityType(blockState);
        if (blockEntityType == null) {
            return;
        }

        BlockEntity blockEntity = blockEntityType.create(renderPos, blockState);
        if (blockEntity == null) {
            return;
        }

        blockEntity.setLevel(level);
        if (!entity.getBlockData().isEmpty()) {
            blockEntity.load(entity.getBlockData());
        }

        renderBlockEntity(blockEntity, poseStack, buffer, packedLight);
    }

    private BlockEntityType<?> findBlockEntityType(BlockState blockState) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.stream()
                .filter(type -> type.isValid(blockState))
                .findFirst()
                .orElse(null);
    }

    private <T extends BlockEntity> void renderBlockEntity(T blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockEntityRenderer<T> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }

        poseStack.pushPose();
        try {
            renderer.render(blockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CustomFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
