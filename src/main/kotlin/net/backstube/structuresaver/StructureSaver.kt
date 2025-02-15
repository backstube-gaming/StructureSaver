package net.backstube.structuresaver

import net.backstube.structuresaver.components.StructureSaverComponents
import net.backstube.structuresaver.networking.Packets
import net.backstube.structuresaver.servernetworking.MessageReceiver
import net.backstube.structuresaver.structureblock.ExporterScreenHandler
import net.backstube.structuresaver.structureblock.ExtendedStructureBlock
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.backstube.structuresaver.structureloader.LoaderScreenHandler
import net.backstube.structuresaver.structureloader.StructureLoaderBlock
import net.backstube.structuresaver.structureloader.StructureLoaderBlockEntity
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.fabricmc.api.ModInitializer
import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.CheckedRandom
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.SecureRandom

object StructureSaver : ModInitializer {

    const val MODID = "structuresaver"
    val logger: Logger = LoggerFactory.getLogger(MODID)
    val random = CheckedRandom(SecureRandom().nextLong())
    private var clientSideHooks: ClientSideHooks = ClientSideHooks()


    object Entries {
        val StructureSaverItem = StructureSaverItem(Item.Settings())

        val ExtendedStructureBlock = ExtendedStructureBlock(
            Settings.create().pistonBehavior(
                PistonBehavior.BLOCK
            )
        )
        val ExtendedStructureBlockItem = BlockItem(ExtendedStructureBlock, Item.Settings())
        val ExtendedStructureBlockEntityType: BlockEntityType<ExtendedStructureBlockEntity> = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MODID, "structure_export_blockentity"),
            BlockEntityType.Builder.create(::ExtendedStructureBlockEntity, ExtendedStructureBlock).build()
        )

        val StructureLoaderBlock = StructureLoaderBlock(
            Settings.create().pistonBehavior(
                PistonBehavior.BLOCK
            )
        )
        val StructureLoaderBlockItem = BlockItem(StructureLoaderBlock, Item.Settings())
        val StructureLoaderBlockEntityType: BlockEntityType<StructureLoaderBlockEntity> = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MODID, "structure_loader_blockentity"),
            BlockEntityType.Builder.create(::StructureLoaderBlockEntity, StructureLoaderBlock).build()
        )
    }

    override fun onInitialize() {
        StructureSaverComponents.register()
        Packets.register()
        Registry.register(Registries.ITEM, Identifier(MODID, "structure_saver"), Entries.StructureSaverItem)

        Registry.register(
            Registries.BLOCK,
            Identifier(MODID, "structure_export_block"),
            Entries.ExtendedStructureBlock
        )
        Registry.register(
            Registries.ITEM,
            Identifier(MODID, "structure_export_blockitem"),
            Entries.ExtendedStructureBlockItem
        )
        ExporterScreenHandler.register()

        Registry.register(
            Registries.BLOCK,
            Identifier(MODID, "structure_loader_block"),
            Entries.StructureLoaderBlock
        )
        Registry.register(
            Registries.ITEM,
            Identifier(MODID, "structure_loader_blockitem"),
            Entries.StructureLoaderBlockItem
        )
        LoaderScreenHandler.register()

        MessageReceiver.setupReceivers()
    }

    fun registerClientHooks(clientSideHooks: ClientSideHooks) {
        this.clientSideHooks = clientSideHooks
    }

    fun getClientHooks(): ClientSideHooks {
        return this.clientSideHooks
    }
}