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
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureTemplate
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random
import org.joml.Vector3f

/**
 * Copy of Vanilla Structure Block with no range limitation and no corner mode
 */
class ExtendedStructureBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ExtendedStructureBlockEntityType, pos, state), ExtendedScreenHandlerFactory<ExtendedStructureData> {

    private var author = ""

    public val data = getInitialData()

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        // only the server has the property delegate at first
        // the client will start with an empty one and then sync
        val data = getInitialData();
        data.pos = this.pos;
        return ExporterScreenHandler(syncId, PlayerInventory(player), data)
    }

    override fun getDisplayName(): Text {
        return Text.translatable("block.structuresaver.structure_export_block")
    }

    override fun getScreenOpeningData(player: ServerPlayerEntity?): ExtendedStructureData {
        return data;
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        nbt.putString("author", this.author)

        nbt.putString("name", data.name)
        nbt.putInt("posX", data.offset.x)
        nbt.putInt("posY", data.offset.y)
        nbt.putInt("posZ", data.offset.z)
        nbt.putFloat("sizeX", data.size.x)
        nbt.putFloat("sizeY", data.size.y)
        nbt.putFloat("sizeZ", data.size.z)
        nbt.putBoolean("shouldIncludeEntities", data.shouldIncludeEntities)
        nbt.putBoolean("shouldIgnoreAir", data.shouldIgnoreAir)
        nbt.putBoolean("shouldSaveOnServer", data.shouldSaveOnServer)
        nbt.putBoolean("showBoundingBox", data.showBoundingBox)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        this.author = nbt.getString("author")
        data.name =  nbt.getString("name")
        data.offset = BlockPos(nbt.getInt("posX"),
            nbt.getInt("posY"),
            nbt.getInt("posZ"))
        data.size = Vector3f(nbt.getFloat("sizeX"),
            nbt.getFloat("sizeY"),
            nbt.getFloat("sizeZ"))
        data.shouldIncludeEntities =  nbt.getBoolean("shouldIncludeEntities")
        data.shouldIgnoreAir =  nbt.getBoolean("shouldIgnoreAir")
        data.shouldSaveOnServer =  nbt.getBoolean("shouldSaveOnServer")
        data.showBoundingBox =  nbt.getBoolean("showBoundingBox")
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket? {
        return BlockEntityUpdateS2CPacket.create(this)
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
                Vec3i(data.size.x.toInt(), data.size.y.toInt(), data.size.z.toInt()),
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
        fun getInitialData(): ExtendedStructureData {
           return ExtendedStructureData(
                BlockPos.ORIGIN, // this should hopefully never be used
                "",
                BlockPos(0, 1, 0),
                Vector3f(0f),
                shouldIncludeEntities = false,
                shouldIgnoreAir = true,
                shouldSaveOnServer = false,
                showBoundingBox = true
            )
        }
    }
}