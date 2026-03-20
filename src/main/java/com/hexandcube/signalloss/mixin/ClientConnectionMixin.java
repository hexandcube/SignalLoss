package com.hexandcube.signalloss.mixin;

import com.hexandcube.signalloss.client.SignalLossClient;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onPacketReceived(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        SignalLossClient.lastPacketTime = System.nanoTime();
    }
}