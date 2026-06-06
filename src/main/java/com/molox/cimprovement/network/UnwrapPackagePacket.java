package com.molox.cimprovement.network;

import com.molox.cimprovement.handler.PackageUnwrapHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 → 服务端：请求对某个 slot 中的包裹执行拆包操作。
 */
public record UnwrapPackagePacket(int slotIndex) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            "create_improvement", "unwrap_package"
    );
    public static final Type<UnwrapPackagePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UnwrapPackagePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeVarInt(pkt.slotIndex),
                    buf -> new UnwrapPackagePacket(buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnwrapPackagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            int slotIndex = packet.slotIndex();
            var menu = player.containerMenu;
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;

            ItemStack stack = menu.slots.get(slotIndex).getItem();
            PackageUnwrapHandler.unwrapPackage(player, stack, slotIndex);
        });
    }
}