package net.backstube.structuresaver

import net.backstube.structuresaver.renderer.ExtendedStructureBlockEntityRenderer
import net.backstube.structuresaver.screens.ExtendedStructureBlockScreen
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockScreenHandler
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

@Suppress("unused")
object StructureSaverClient : ClientModInitializer {
	override fun onInitializeClient() {
		StructureSaver.registerClientHooks(ClientHooks())

		// the generics are required, because the inference does not work correctly!
		@Suppress("RemoveExplicitTypeArguments")
		HandledScreens.register<ExtendedStructureBlockScreenHandler, ExtendedStructureBlockScreen>(
			ExtendedStructureBlockScreenHandler.EXTENDED_SCREEN_HANDLER
		) { handler, _, _ -> ExtendedStructureBlockScreen(handler) }

		BlockEntityRendererFactories.register(StructureSaver.Entries.ExtendedStructureBlockEntityType, ::ExtendedStructureBlockEntityRenderer);
	}
}