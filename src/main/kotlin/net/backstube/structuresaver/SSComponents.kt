package net.backstube.structuresaver

import net.backstube.structuresaver.structureloader.StructureLoaderData
import net.backstube.structuresaver.structuresaveritem.SaverItemData
import net.minecraft.component.DataComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class SSComponents {

    companion object{
        fun register(){}

        val SAVER_ITEM_COMPONENT: DataComponentType<SaverItemData> = Registry.register<DataComponentType<*>, DataComponentType<SaverItemData>>(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(StructureSaver.MODID, "saver_item_component"),
            DataComponentType.builder<SaverItemData>().codec(SaverItemData.CODEC).build()
        )

        val LOADER_COMPONENT: DataComponentType<StructureLoaderData> = Registry.register<DataComponentType<*>, DataComponentType<StructureLoaderData>>(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(StructureSaver.MODID, "loader_component"),
            DataComponentType.builder<StructureLoaderData>().codec(StructureLoaderData.CODEC).build()
        )
    }
}