package net.backstube.structuresaver.servernetworking

import io.netty.buffer.Unpooled
import net.backstube.structuresaver.networking.Packets
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object MessageSender {
    fun openScreen(player: ServerPlayerEntity, stack: ItemStack?) {
        val passedData = PacketByteBuf(Unpooled.buffer())
        passedData.writeItemStack(stack)
        ServerPlayNetworking.send(player, Packets.S2C_OPEN_SCREEN, passedData)
    }
}