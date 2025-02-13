package net.backstube.structuresaver.clientnetworking

import net.backstube.structuresaver.screens.StructureSaverScreen
import net.backstube.structuresaver.networking.Packets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

object MessageReceiver {
   /* fun setupReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_OPEN_SCREEN
        ) { client: MinecraftClient, handler: ClientPlayNetworkHandler?, buf: PacketByteBuf, responseSender: PacketSender? ->
            val item = buf.readItemStack()
            client.execute {
                MinecraftClient.getInstance().setScreen(StructureSaverScreen(item, Text.translatable("screen.structuresaver.structure_saver")))
            }
        }
    }*/
}