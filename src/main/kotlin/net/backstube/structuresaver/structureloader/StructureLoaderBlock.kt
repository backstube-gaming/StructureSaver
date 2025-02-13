package net.backstube.structuresaver.structureloader

import com.mojang.serialization.MapCodec
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.enums.StructureBlockMode
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties.STRUCTURE_BLOCK_MODE
import net.minecraft.state.property.Property
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class StructureLoaderBlock(settings: Settings?) : BlockWithEntity(settings), OperatorBlock {


    companion object {
        val MODE: EnumProperty<StructureBlockMode> = STRUCTURE_BLOCK_MODE
        val CODEC: MapCodec<StructureLoaderBlock> =
            createCodec { settings: Settings? -> StructureLoaderBlock(settings) }
    }

    init {
        this.defaultState = (this.stateManager.defaultState as BlockState).with(MODE, StructureBlockMode.LOAD)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState?): BlockEntity {
        return StructureLoaderBlockEntity(pos, state!!)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hit: BlockHitResult?
    ): ActionResult {
        if (player == null)
            return ActionResult.FAIL

        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is StructureLoaderBlockEntity && state != null) {
            if (!world.isClient) {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            }
            return ActionResult.SUCCESS

        } else {
            return ActionResult.PASS
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if (!world.isClient) {
            if (placer != null) {
                val blockEntity = world.getBlockEntity(pos)
                if (blockEntity is StructureLoaderBlockEntity) {
                    blockEntity.setAuthor(placer)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(*arrayOf<Property<*>?>(MODE))
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> {
        return CODEC
    }
}