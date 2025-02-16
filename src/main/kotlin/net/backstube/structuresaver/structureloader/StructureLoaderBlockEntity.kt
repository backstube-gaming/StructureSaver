package net.backstube.structuresaver.structureloader

import com.google.gson.Gson
import net.backstube.structuresaver.CodecHelper
import net.backstube.structuresaver.SSComponents
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.StructureSaver.Entries.StructureLoaderBlockEntityType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.component.ComponentMap
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

/**
 * Copy of Vanilla Structure Block with no range limitation and no corner mode
 */
class StructureLoaderBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(StructureLoaderBlockEntityType, pos, state), ExtendedScreenHandlerFactory<StructureLoaderData> {

    private var author = ""
    var data: StructureLoaderData? = null

    private fun getScreenData(): StructureLoaderData{
        val component = data ?: getInitialData()
        if (component.name == "" || component.pos == BlockPos.ORIGIN) {
            val gson = Gson()
            StructureSaver.logger.error(
                "Broken structure_loader data at {}. The loader wont work properly. Data: {}",
                this.pos,
                gson.toJson(data)
            )
        }
        component.pos = this.pos
        return component
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        // only the server has the property delegate at first
        // the client will start with an empty one and then sync
        return LoaderScreenHandler(syncId, PlayerInventory(player), getScreenData())
    }

    override fun getDisplayName(): Text {
        return Text.translatable("block.structuresaver.structure_loader_block")
    }

    override fun getScreenOpeningData(player: ServerPlayerEntity?): StructureLoaderData {
        return getScreenData()
    }

    override fun addComponents(componentMapBuilder: ComponentMap.Builder) {
        super.addComponents(componentMapBuilder)
        componentMapBuilder.add(SSComponents.LOADER_COMPONENT, data)
    }

    override fun readComponents(components: ComponentsAccess) {
        super.readComponents(components)
        if (data == null) {
            val initial = getInitialData()
            data = components.getOrDefault(
                SSComponents.LOADER_COMPONENT,
                StructureLoaderData(this.pos, initial.name, initial.shouldIncludeEntities, initial.direction)
            )
        }
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        nbt.putString(AUTHOR_KEY, this.author)

        data = data ?: getInitialData()
        val encoded = CodecHelper.encodeNbt(
            StructureLoaderData.CODEC, data!!, "StructureLoaderBlockEntity",
            pos.toString(), StructureSaver.logger
        )
        if (encoded != null) {
            nbt.put(DATA_KEY, encoded)
        }
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        this.author = nbt.getString(AUTHOR_KEY)

        if (!nbt.contains(DATA_KEY))
            return

        val decoded = nbt[DATA_KEY]?.let {
            CodecHelper.decodeNbt(
                StructureLoaderData.CODEC,
                it,
                "StructureLoaderBlockEntity",
                pos.toString(),
                StructureSaver.logger
            )
        }!!
        // in case something wrote the initial data
        if (decoded.pos == BlockPos.ORIGIN)
            decoded.pos = this.pos
        data = decoded
    }

    // Properties that are transferred to the clients for visuals
    // Warning: Need to call world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    // to trigger the update, otherwise the client does not know that the block entity has been changed.
    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    // nbt that is required on the client for rendering purposes e.g. like signs. normally nbt is only sent when the screen is opened
    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound {
        return createNbt(registryLookup)
    }

    fun setAuthor(entity: LivingEntity) {
        this.author = entity.name.string
    }

    companion object {
        const val AUTHOR_KEY: String = "author"
        const val DATA_KEY = "structureloader"

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