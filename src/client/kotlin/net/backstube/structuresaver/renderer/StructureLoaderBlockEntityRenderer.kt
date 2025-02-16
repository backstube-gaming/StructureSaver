package net.backstube.structuresaver.renderer

import net.backstube.structuresaver.structureloader.StructureLoaderBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos

/*
* Copy of Vanilla class to show bounding box
*/
class StructureLoaderBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<StructureLoaderBlockEntity> {


    override fun render(
        loaderBlockEntity: StructureLoaderBlockEntity,
        f: Float,
        matrixStack: MatrixStack?,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        j: Int
    ) {
        if (!MinecraftClient.getInstance().player!!.isCreativeLevelTwoOp && !MinecraftClient.getInstance().player!!.isSpectator) {
            return
        }
        if(loaderBlockEntity.data == null)
            return
        val blockPos = BlockPos(0, 1, 0)
        val x = blockPos.x.toDouble()
        val y = blockPos.y.toDouble()
        val z = blockPos.z.toDouble()

        val endpointX: Double
        val endpointY: Double
        val endpointZ: Double

        when (loaderBlockEntity.data!!.direction) {
            0 -> { // east-south
                endpointX = 16.0
                endpointY = 17.0
                endpointZ = 16.0
            }
            1 -> { // west-south
                endpointX = -16.0
                endpointY = 17.0
                endpointZ = 16.0
            }
            2 -> { // west-north
                endpointX = -16.0
                endpointY = 17.0
                endpointZ = -16.0
            }
            else -> { // east-north
                endpointX = 16.0
                endpointY = 17.0
                endpointZ = -16.0
            }
        }

        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines())
        WorldRenderer.drawBox(
            matrixStack,
            vertexConsumer,
            x,
            y,
            z,
            endpointX,
            endpointY,
            endpointZ,
            0.9f,
            0.9f,
            0.9f,
            1.0f,
            0.5f,
            0.5f,
            0.5f
        )
    }


    override fun rendersOutsideBoundingBox(structureBlockBlockEntity: StructureLoaderBlockEntity?): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 2048
    }
}

