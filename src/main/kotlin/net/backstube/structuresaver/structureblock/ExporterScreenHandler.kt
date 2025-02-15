package net.backstube.structuresaver.structureblock

import net.backstube.structuresaver.StructureSaver
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.joml.Vector3f

class ExporterScreenHandler(
    syncId: Int,
    var blockEntity: ExtendedStructureBlockEntity?,
    var data: ExtendedStructureData
) : ScreenHandler(EXTENDED_SCREEN_HANDLER, syncId) {


    companion object {
        fun register() {}
        var EXTENDED_SCREEN_HANDLER: ExtendedScreenHandlerType<ExporterScreenHandler, ExtendedStructureData> =
            Registry.register(
                Registries.SCREEN_HANDLER, Identifier(StructureSaver.MODID, "exporter_handler"),
                ExtendedScreenHandlerType(::ExporterScreenHandler, ExtendedStructureData.PACKET_CODEC)
            )

        private fun getBlockEntity(world: World, pos: BlockPos): ExtendedStructureBlockEntity? {
            val blockEntity: BlockEntity? = world.getBlockEntity(pos)
            return blockEntity as? ExtendedStructureBlockEntity;
        }
    }

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    constructor(syncId: Int, inventory: PlayerInventory, data: ExtendedStructureData) : this(
        syncId,
        getBlockEntity(inventory.player.world, data.pos),
        data,
    ) {
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }

    public fun saveToBlockEntity() {
        blockEntity?.data?.name = this.data.name
        blockEntity?.data?.offset = this.data.offset
        blockEntity?.data?.size = this.data.size
        blockEntity?.data?.shouldIncludeEntities = this.data.shouldIncludeEntities
        blockEntity?.data?.shouldIgnoreAir = this.data.shouldIgnoreAir
        blockEntity?.data?.shouldSaveOnServer = this.data.shouldSaveOnServer
        blockEntity?.data?.pos = this.data.pos
        blockEntity?.data?.showBoundingBox = this.data.showBoundingBox
    }
}
