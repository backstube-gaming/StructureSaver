package net.backstube.structuresaver.structureblock

import net.backstube.structuresaver.StructureSaver.Entries.ExtendedStructureBlockEntityType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureTemplate
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random

/**
 * Copy of Vanilla Structure Block with no range limitation and no corner mode
 */
class ExtendedStructureBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ExtendedStructureBlockEntityType, pos, state), ExtendedScreenHandlerFactory {

    private var author = ""

    public val data = ExtendedStructureData(
        pos,
        "",
        BlockPos(0, 1, 0),
        Vec3i.ZERO,
        shouldIncludeEntities = true,
        shouldIgnoreAir = true,
        shouldSaveOnServer = false,
        showBoundingBox = true
        )

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        // only the server has the property delegate at first
        // the client will start with an empty one and then sync
        return ExtendedStructureBlockScreenHandler(syncId, PlayerInventory(player), this.pos, this, ArrayPropertyDelegate(0))
    }

    override fun getDisplayName(): Text {
        return Text.translatable("block.structuresaver.extended_structure_block")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf) {
        buf.writeBlockPos(data.pos)
        buf.writeString(data.name)
        buf.writeBlockPos(data.offset)
        val size = Vec3d(data.size.x.toDouble(), data.size.y.toDouble(), data.size.z.toDouble())
        buf.writeVec3d(size)
        buf.writeBoolean(data.shouldIncludeEntities)
        buf.writeBoolean(data.shouldIgnoreAir)
        buf.writeBoolean(data.shouldSaveOnServer)
        buf.writeBoolean(data.showBoundingBox)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putString("author", this.author)

        nbt.putString("name", data.name)
        nbt.putInt("posX", data.offset.x)
        nbt.putInt("posY", data.offset.y)
        nbt.putInt("posZ", data.offset.z)
        nbt.putInt("sizeX", data.size.x)
        nbt.putInt("sizeY", data.size.y)
        nbt.putInt("sizeZ", data.size.z)
        nbt.putBoolean("shouldIncludeEntities", data.shouldIncludeEntities)
        nbt.putBoolean("shouldIgnoreAir", data.shouldIgnoreAir)
        nbt.putBoolean("shouldSaveOnServer", data.shouldSaveOnServer)
        nbt.putBoolean("showBoundingBox", data.showBoundingBox)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        this.author = nbt.getString("author")
        data.name =  nbt.getString("name")
        data.offset = BlockPos(nbt.getInt("posX"),
            nbt.getInt("posY"),
            nbt.getInt("posZ"))
        data.size = Vec3i(nbt.getInt("sizeX"),
            nbt.getInt("sizeY"),
            nbt.getInt("sizeZ"))
        data.shouldIncludeEntities =  nbt.getBoolean("shouldIncludeEntities")
        data.shouldIgnoreAir =  nbt.getBoolean("shouldIgnoreAir")
        data.shouldSaveOnServer =  nbt.getBoolean("shouldSaveOnServer")
        data.showBoundingBox =  nbt.getBoolean("showBoundingBox")
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return this.createNbt()
    }

//    fun openScreen(player: PlayerEntity): Boolean {
//        if (!player.isCreativeLevelTwoOp) {
//            return false
//        } else {
//            if (player.entityWorld.isClient) {
//
//                getClientHooks().openExtendedStructureBlockScreen(this)
//            }
//            return true
//        }
//    }

    fun setAuthor(entity: LivingEntity) {
        this.author = entity.name.string
    }

    fun saveStructure(interactive: Boolean = true): Boolean {
        if (!world!!.isClient && data.name.isNotBlank()) {
            val blockPos = getPos().add(data.offset)
            val serverWorld = world as ServerWorld?
            val structureTemplateManager = serverWorld!!.structureTemplateManager

            val templateName = Identifier.tryParse(data.name)
            val structureTemplate: StructureTemplate
            try {
                structureTemplate = structureTemplateManager.getTemplateOrBlank(templateName)
            } catch (var8: InvalidIdentifierException) {
                return false
            }

            structureTemplate.saveFromWorld(
                this.world,
                blockPos,
                data.size,
                !data.shouldIncludeEntities,
                Blocks.STRUCTURE_VOID
            )
            structureTemplate.author = author
            return if (interactive) {
                try {
                    structureTemplateManager.saveTemplate(templateName)
                } catch (var7: InvalidIdentifierException) {
                    false
                }
            } else {
                true
            }
        } else {
            return false
        }
    }

    companion object {
        const val AUTHOR_KEY: String = "author"
        fun createRandom(seed: Long): Random {
            return if (seed == 0L) Random.create(Util.getMeasuringTimeMs()) else Random.create(seed)
        }
    }
}