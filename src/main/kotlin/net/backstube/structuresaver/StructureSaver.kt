package net.backstube.structuresaver

import net.backstube.structuresaver.servernetworking.MessageReceiver
import net.backstube.structuresaver.structureblock.ExtendedStructureBlock
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockScreenHandler
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
import org.slf4j.LoggerFactory

object StructureSaver : ModInitializer {

    const val MODID = "structuresaver"
    private val logger = LoggerFactory.getLogger(MODID)
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
            Identifier(MODID, "extended_structure_blockentity"),
            FabricBlockEntityTypeBuilder.create(::ExtendedStructureBlockEntity, ExtendedStructureBlock).build()
        )
    }

    override fun onInitialize() {
        Registry.register(Registries.ITEM, Identifier(MODID, "structure_saver"), Entries.StructureSaverItem)
        Registry.register(
            Registries.BLOCK,
            Identifier(MODID, "extended_structure_block"),
            Entries.ExtendedStructureBlock
        )
        Registry.register(
            Registries.ITEM,
            Identifier(MODID, "extended_structure_blockitem"),
            Entries.ExtendedStructureBlockItem
        );
        ExtendedStructureBlockScreenHandler.register()
        MessageReceiver.setupReceivers()
    }

    public fun registerClientHooks(clientSideHooks: ClientSideHooks) {
        this.clientSideHooks = clientSideHooks
    }

    public fun getClientHooks(): ClientSideHooks {
        return this.clientSideHooks
    }
}