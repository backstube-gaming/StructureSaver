package net.backstube.structuresaver

import net.backstube.structuresaver.screens.StructureSaverScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class ClientHooks : ClientSideHooks() {
    override fun openStructureSaverScreen(item: ItemStack) {
        MinecraftClient.getInstance().setScreen(StructureSaverScreen(item, Text.translatable("screen.structuresaver.structure_saver")))
    }
}