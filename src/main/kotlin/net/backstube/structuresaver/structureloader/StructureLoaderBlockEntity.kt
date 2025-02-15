package net.backstube.structuresaver.structureloader

import net.backstube.structuresaver.StructureSaver.Entries.StructureLoaderBlockEntityType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

/**
 * Copy of Vanilla Structure Block with no range limitation and no corner mode
 */
class StructureLoaderBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(StructureLoaderBlockEntityType, pos, state), ExtendedScreenHandlerFactory<StructureLoaderData> {

    private var author = ""
    public var data = getInitialData()

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        // only the server has the property delegate at first
        // the client will start with an empty one and then sync
        val data = getInitialData();
        data.pos = this.pos;
        return LoaderScreenHandler(syncId, PlayerInventory(player), data)
    }

    override fun getDisplayName(): Text {
        return Text.translatable("block.structuresaver.structure_loader_block")
    }

    override fun getScreenOpeningData(player: ServerPlayerEntity?): StructureLoaderData {
        return data;
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        nbt.putString("author", this.author)

        nbt.putString("name", data.name)
        nbt.putBoolean("shouldIncludeEntities", data.shouldIncludeEntities)
        nbt.putInt("direction", data.direction)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        this.author = nbt.getString("author")
        data.name = nbt.getString("name")
        data.shouldIncludeEntities = nbt.getBoolean("shouldIncludeEntities")
        data.direction = nbt.getInt("direction")
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    fun setAuthor(entity: LivingEntity) {
        this.author = entity.name.string
    }

    companion object {
        const val AUTHOR_KEY: String = "author"
        fun createRandom(seed: Long): Random {
            return if (seed == 0L) Random.create(Util.getMeasuringTimeMs()) else Random.create(seed)
        }

        fun getInitialData(): StructureLoaderData {
            return StructureLoaderData(
                BlockPos.ORIGIN, // this should hopefully never be used
                "",
                shouldIncludeEntities = false,
                direction = 0
            )
        }
    }
}