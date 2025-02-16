package net.backstube.structuresaver.renderer

import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

/*
* Copy of Vanilla class to show bounding box
*/
class ExtendedStructureBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<ExtendedStructureBlockEntity> {


    override fun render(
        exportBlockEntity: ExtendedStructureBlockEntity,
        f: Float,
        matrixStack: MatrixStack?,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        j: Int
    ) {
        val o: Double
        val n: Double
        val m: Double
        val k: Double
        if (!MinecraftClient.getInstance().player!!.isCreativeLevelTwoOp && !MinecraftClient.getInstance().player!!.isSpectator) {
            return
        }
        val blockPos = exportBlockEntity.data.offset
        val vec3i = exportBlockEntity.data.size
        if (vec3i.x < 1 || vec3i.y < 1 || vec3i.z < 1) {
            return
        }
        val d = blockPos.x.toDouble()
        val e = blockPos.z.toDouble()
        val g = blockPos.y.toDouble()
        val h = g + vec3i.y.toDouble()
        k = vec3i.x.toDouble()
        val l = vec3i.z.toDouble()
        m = if (k < 0.0) d + 1.0 else d
        n = if (l < 0.0) e + 1.0 else e
        o = m + k
        val p = n + l
        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines())
            WorldRenderer.drawBox(
                matrixStack,
                vertexConsumer,
                m,
                g,
                n,
                o,
                h,
                p,
                0.9f,
                0.9f,
                0.9f,
                1.0f,
                0.5f,
                0.5f,
                0.5f
            )
    }


    override fun rendersOutsideBoundingBox(structureBlockBlockEntity: ExtendedStructureBlockEntity?): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 10000
    }
}

