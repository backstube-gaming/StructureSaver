package net.backstube.structuresaver.components

import net.backstube.structuresaver.StructureSaver
import net.minecraft.component.DataComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class StructureSaverComponents {

    companion object{
        fun register(){}

        val SAVER_ITEM_COMPONENT: DataComponentType<SaverItemData> = Registry.register<DataComponentType<*>, DataComponentType<SaverItemData>>(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(StructureSaver.MODID, "saver_item_component"),
            DataComponentType.builder<SaverItemData>().codec(SaverItemData.CODEC).build()
        )
    }
}