package de.melanx.skyguis.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import de.melanx.skyguis.SkyGUIs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.ModelData;
import org.moddingx.libx.render.ClientTickHandler;

import java.util.*;

/*
 * Most code taken from Patchouli by Vazkii
 * https://github.com/VazkiiMods/Patchouli/blob/35ae32b6b9c9c37a78ecd4867b83ba25304fd0c7/Common/src/main/java/vazkii/patchouli/client/book/page/PageMultiblock.java
 * The license of Patchouli applies to this I guess.
 */
public class TemplateRenderer {

    private final RandomSource random = RandomSource.create();
    private final ClientLevel clientLevel = Objects.requireNonNull(Minecraft.getInstance().level);
    private final StructureTemplate template;
    private final float maxX;
    private final float maxY;
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
    private final transient Set<BlockEntity> erroredTiles = Collections.newSetFromMap(new WeakHashMap<>());

    public TemplateRenderer(StructureTemplate template, float maxSize) {
        this(template, maxSize, maxSize);
    }

    public TemplateRenderer(StructureTemplate template, float maxX, float maxY) {
        this.template = template;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void render(PoseStack poseStack, int xPos, int yPos) {
        Vec3i size = this.template.getSize();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        float diag = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ);
        float scaleX = this.maxX / diag;
        float scaleY = this.maxY / sizeY;
        float scale = -Math.min(scaleX, scaleY);

        poseStack.pushPose();
        poseStack.translate(xPos, yPos, 100);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-(float) sizeX / 2, -(float) sizeY / 2, 0);

        // Initial eye pos somewhere off in the distance in the -Z direction
        Vector4f eye = new Vector4f(0, 0, -100, 1);
        Matrix4f rotMat = new Matrix4f();
        rotMat.setIdentity();

        // For each GL rotation done, track the opposite to keep the eye pos accurate
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-30F));
        rotMat.multiply(Vector3f.XP.rotationDegrees(30));

        float offX = (float) -sizeX / 2;
        float offZ = (float) -sizeZ / 2 + 1;

        float time = ClientTickHandler.ticksInGame;
        poseStack.translate(-offX, 0, -offZ);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(time));
        rotMat.multiply(Vector3f.YP.rotationDegrees(-time));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(45));
        rotMat.multiply(Vector3f.YP.rotationDegrees(-45));
        poseStack.translate(offX, 0, offZ);

        // Finally apply the rotations
        eye.transform(rotMat);
        eye.perspectiveDivide();
        this.renderElements(poseStack, this.template);

        poseStack.popPose();
    }

    private void renderElements(PoseStack poseStack, StructureTemplate template) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.translate(0, 0, -1);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        this.doWorldRenderPass(poseStack, template, buffers);
        this.doTileEntityRenderPass(poseStack, template, buffers);

        buffers.endBatch();
        poseStack.popPose();
    }

    private void doWorldRenderPass(PoseStack poseStack, StructureTemplate template, MultiBufferSource.BufferSource buffers) {
        for (StructureTemplate.Palette palette : template.palettes) {
            for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                BlockPos pos = blockInfo.pos;
                BlockState bs = blockInfo.state;
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                this.renderForMultiblock(bs, pos, poseStack, buffers);
                poseStack.popPose();
            }
        }
    }

    private void renderForMultiblock(BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = blockRenderer.getBlockModel(state);
            for (RenderType layer : model.getRenderTypes(state, this.random, ModelData.EMPTY)) {
                VertexConsumer buffer = buffers.getBuffer(layer);
                blockRenderer.renderBatched(state, pos, this.clientLevel, poseStack, buffer, false, this.clientLevel.random, ModelData.EMPTY, layer);
            }
        }
    }

    private void doTileEntityRenderPass(PoseStack poseStack, StructureTemplate template, MultiBufferSource buffers) {
        for (StructureTemplate.Palette palette : template.palettes) {
            for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                BlockPos pos = blockInfo.pos;
                BlockState state = blockInfo.state;

                BlockEntity te = null;
                if (state.getBlock() instanceof EntityBlock) {
                    te = this.teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
                }

                if (te != null && !this.erroredTiles.contains(te)) {
                    te.setLevel(this.clientLevel);

                    // fake cached state in case the renderer checks it as we don't want to query the actual world
                    //noinspection deprecation
                    te.setBlockState(state);

                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    try {
                        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
                        if (renderer != null) {
                            renderer.render(te, 0, poseStack, buffers, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
                        }
                    } catch (Exception e) {
                        this.erroredTiles.add(te);
                        SkyGUIs.getInstance().logger.error("An exception occurred rendering tile entity", e);
                    } finally {
                        poseStack.popPose();
                    }
                }
            }
        }
    }
}
