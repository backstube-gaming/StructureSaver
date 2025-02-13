package net.backstube.structuresaver

import net.backstube.structuresaver.renderer.ExtendedStructureBlockEntityRenderer
import net.backstube.structuresaver.renderer.StructureLoaderBlockEntityRenderer
import net.backstube.structuresaver.screens.ExtendedStructureBlockScreen
import net.backstube.structuresaver.screens.StructureLoaderScreen
import net.backstube.structuresaver.structureblock.ExporterScreenHandler
import net.backstube.structuresaver.structureloader.LoaderScreenHandler
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

@Suppress("unused")
object StructureSaverClient : ClientModInitializer {
	override fun onInitializeClient() {
		StructureSaver.registerClientHooks(ClientHooks())

		// the generics are required, because the inference does not work correctly!
		@Suppress("RemoveExplicitTypeArguments")
		HandledScreens.register<ExporterScreenHandler, ExtendedStructureBlockScreen>(
			ExporterScreenHandler.EXTENDED_SCREEN_HANDLER
		) { handler, _, _ -> ExtendedStructureBlockScreen(handler) }
		BlockEntityRendererFactories.register(StructureSaver.Entries.ExtendedStructureBlockEntityType, ::ExtendedStructureBlockEntityRenderer);

		// the generics are required, because the inference does not work correctly!
		@Suppress("RemoveExplicitTypeArguments")
		HandledScreens.register<LoaderScreenHandler, StructureLoaderScreen>(
			LoaderScreenHandler.EXTENDED_SCREEN_HANDLER
		) { handler, _, _ -> StructureLoaderScreen(handler) }
		BlockEntityRendererFactories.register(StructureSaver.Entries.StructureLoaderBlockEntityType, ::StructureLoaderBlockEntityRenderer);
	}
}