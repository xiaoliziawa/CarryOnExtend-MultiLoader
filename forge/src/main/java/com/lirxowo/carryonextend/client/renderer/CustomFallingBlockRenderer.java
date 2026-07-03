package com.lirxowo.carryonextend.client.renderer;

import com.lirxowo.carryonextend.registry.CustomFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

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
            if (blockState.hasBlockEntity()) {
                renderBlockWithEntity(entity, blockState, level, renderPos, poseStack, buffer, packedLight);
            } else {
                renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, false);
            }
        } finally {
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBlockWithEntity(CustomFallingBlockEntity entity, BlockState blockState, Level level, BlockPos renderPos, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockEntityType<?> blockEntityType = findBlockEntityType(blockState);
        if (blockEntityType == null) {
            renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
            return;
        }

        BlockEntity blockEntity = blockEntityType.create(renderPos, blockState);
        if (blockEntity == null) {
            renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
            return;
        }

        blockEntity.setLevel(level);
        if (!entity.getBlockData().isEmpty()) {
            blockEntity.load(entity.getBlockData());
        }

        if (blockState.getRenderShape() == RenderShape.MODEL) {
            renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
        }

        renderBlockEntity(blockEntity, poseStack, buffer, packedLight);
    }

    private BlockEntityType<?> findBlockEntityType(BlockState blockState) {
        for (BlockEntityType<?> type : ForgeRegistries.BLOCK_ENTITY_TYPES) {
            if (type.isValid(blockState)) {
                return type;
            }
        }
        return null;
    }

    private void renderBlockModel(BlockState blockState, Level level, BlockPos renderPos, PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean translucentMovingBlock) {
        BakedModel model = dispatcher.getBlockModel(blockState);
        RandomSource randomSource = RandomSource.create();
        ModelData modelData = ModelData.EMPTY;
        BlockEntity blockEntity = level.getBlockEntity(renderPos);
        if (blockEntity != null) {
            modelData = blockEntity.getModelData();
        }

        for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
            RenderType actualRenderType = translucentMovingBlock ? RenderType.translucentMovingBlock() : renderType;
            dispatcher.getModelRenderer().tesselateBlock(level, model, blockState, renderPos, poseStack, buffer.getBuffer(actualRenderType), false, randomSource, blockState.getSeed(renderPos), OverlayTexture.NO_OVERLAY, modelData, actualRenderType);
        }
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
