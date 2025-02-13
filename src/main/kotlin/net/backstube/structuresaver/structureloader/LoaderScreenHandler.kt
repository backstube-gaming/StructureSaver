package net.backstube.structuresaver.structureloader

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

class LoaderScreenHandler(
    syncId: Int,
    var blockEntity: StructureLoaderBlockEntity?,
    var data: StructureLoaderData
) : ScreenHandler(EXTENDED_SCREEN_HANDLER, syncId) {

    companion object {
        fun register() {}
        var EXTENDED_SCREEN_HANDLER: ExtendedScreenHandlerType<LoaderScreenHandler, StructureLoaderData> = Registry.register(
            Registries.SCREEN_HANDLER, Identifier(StructureSaver.MODID, "loader_handler"),
            ExtendedScreenHandlerType<LoaderScreenHandler,StructureLoaderData>(::LoaderScreenHandler, StructureLoaderData.PACKET_CODEC)
        )

        private fun getBlockEntity(world: World, pos: BlockPos): StructureLoaderBlockEntity? {
            val blockEntity: BlockEntity? = world.getBlockEntity(pos)
            return if (blockEntity is StructureLoaderBlockEntity)
                blockEntity;
            else
                null;
        }
    }

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    constructor(syncId: Int, inventory: PlayerInventory, data: StructureLoaderData) : this(
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
        blockEntity?.data?.shouldIncludeEntities = this.data.shouldIncludeEntities
        blockEntity?.data?.pos = this.data.pos
        blockEntity?.data?.direction = this.data.direction
    }
}
