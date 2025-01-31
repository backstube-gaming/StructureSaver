package net.backstube.structuresaver

import net.backstube.structuresaver.servernetworking.MessageReceiver
import net.backstube.structuresaver.structureblock.ExtendedStructureBlock
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockScreenHandler
import net.backstube.structuresaver.structureloader.LoaderScreenHandler
import net.backstube.structuresaver.structureloader.StructureLoaderBlock
import net.backstube.structuresaver.structureloader.StructureLoaderBlockEntity
import net.backstube.structuresaver.structureloader.StructureLoaderData
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.BlockItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.CheckedRandom
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import kotlin.random.Random

object StructureSaver : ModInitializer {

    const val MODID = "structuresaver"
    public val logger = LoggerFactory.getLogger(MODID)
    public val random = CheckedRandom(SecureRandom().nextLong())
    private var clientSideHooks: ClientSideHooks = ClientSideHooks()


    public object Entries {
        public val StructureSaverItem = StructureSaverItem(FabricItemSettings())

        public val ExtendedStructureBlock = ExtendedStructureBlock(
            FabricBlockSettings.create().pistonBehavior(
                PistonBehavior.BLOCK
            )
        )
        public val ExtendedStructureBlockItem = BlockItem(ExtendedStructureBlock, FabricItemSettings())
        public val ExtendedStructureBlockEntityType: BlockEntityType<ExtendedStructureBlockEntity> = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MODID, "structure_export_blockentity"),
            FabricBlockEntityTypeBuilder.create(::ExtendedStructureBlockEntity, ExtendedStructureBlock).build()
        )

        public val StructureLoaderBlock = StructureLoaderBlock(
            FabricBlockSettings.create().pistonBehavior(
                PistonBehavior.BLOCK
            )
        )
        public val StructureLoaderBlockItem = BlockItem(StructureLoaderBlock, FabricItemSettings())
        public val StructureLoaderBlockEntityType: BlockEntityType<StructureLoaderBlockEntity> = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(MODID, "structure_loader_blockentity"),
            FabricBlockEntityTypeBuilder.create(::StructureLoaderBlockEntity, StructureLoaderBlock).build()
        )
    }

    override fun onInitialize() {
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
        );
        ExtendedStructureBlockScreenHandler.register()

        Registry.register(
            Registries.BLOCK,
            Identifier(MODID, "structure_loader_block"),
            Entries.StructureLoaderBlock
        )
        Registry.register(
            Registries.ITEM,
            Identifier(MODID, "structure_loader_blockitem"),
            Entries.StructureLoaderBlockItem
        );
        LoaderScreenHandler.register()

        MessageReceiver.setupReceivers()
    }

    public fun registerClientHooks(clientSideHooks: ClientSideHooks) {
        this.clientSideHooks = clientSideHooks
    }

    public fun getClientHooks(): ClientSideHooks {
        return this.clientSideHooks
    }
}