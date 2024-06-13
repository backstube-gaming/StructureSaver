package net.backstube.structuresaver.structureblock

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
import net.minecraft.util.math.Vec3i

class ExtendedStructureBlockScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory?,
    val blockPos: BlockPos,
    var blockEntity: ExtendedStructureBlockEntity?,
    val propertyDelegate: PropertyDelegate
) : ScreenHandler(EXTENDED_SCREEN_HANDLER, syncId) {

    public var data: ExtendedStructureData = ExtendedStructureData(
        BlockPos.ORIGIN, // this should hopefully never be used
        "",
        BlockPos(0, 1, 0),
        Vec3i.ZERO,
        shouldIncludeEntities = false,
        shouldIgnoreAir = true,
        shouldSaveOnServer = false,
        showBoundingBox = true
    )

    companion object {
        fun register() {}
        var EXTENDED_SCREEN_HANDLER: ExtendedScreenHandlerType<ExtendedStructureBlockScreenHandler> = Registry.register(
            Registries.SCREEN_HANDLER, Identifier(StructureSaver.MODID, "exporter_handler"),
            ExtendedScreenHandlerType<ExtendedStructureBlockScreenHandler>(::ExtendedStructureBlockScreenHandler)
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
        if (genericEntity is ExtendedStructureBlockEntity)
            this.blockEntity = genericEntity
        val name = buf.readString()
        val offset = buf.readBlockPos()
        val size = buf.readVec3d()
        val shouldIncludeEntities = buf.readBoolean()
        val shouldIgnoreAir = buf.readBoolean()
        val shouldSaveOnServer = buf.readBoolean()
        val showBoundingBox = buf.readBoolean()

        data = ExtendedStructureData(
            pos, name, offset,
            Vec3i(size.x.toInt(), size.y.toInt(), size.z.toInt()),
            shouldIncludeEntities, shouldIgnoreAir, shouldSaveOnServer, showBoundingBox
        )
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }

    public fun saveToBlockEntity(){
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
