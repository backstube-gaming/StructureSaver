package net.backstube.structuresaver.structureloader

import net.backstube.structuresaver.StructureSaver
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class LoaderScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory?,
    val blockPos: BlockPos,
    var blockEntity: StructureLoaderBlockEntity?,
    val propertyDelegate: PropertyDelegate
) : ScreenHandler(EXTENDED_SCREEN_HANDLER, syncId) {

    public var data: StructureLoaderData = StructureLoaderData(
        BlockPos.ORIGIN, // this should hopefully never be used
        "",
        shouldIncludeEntities = false,
    )

    companion object {
        fun register() {}
        var EXTENDED_SCREEN_HANDLER: ExtendedScreenHandlerType<LoaderScreenHandler> = Registry.register(
            Registries.SCREEN_HANDLER, Identifier(StructureSaver.MODID, "loader_handler"),
            ExtendedScreenHandlerType<LoaderScreenHandler>(::LoaderScreenHandler)
        )
    }

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf) : this(
        syncId,
        inventory,
        buf.readBlockPos(),
        null,
        ArrayPropertyDelegate(0)
    ) {
        val pos = this.blockPos; // already read from buffer
        val genericEntity = inventory.player.world.getBlockEntity(pos)
        if (genericEntity is StructureLoaderBlockEntity)
            this.blockEntity = genericEntity
        val name = buf.readString()
        val shouldIncludeEntities = buf.readBoolean()

        data = StructureLoaderData(
            pos,
            name,
            shouldIncludeEntities
        )
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
    }
}
