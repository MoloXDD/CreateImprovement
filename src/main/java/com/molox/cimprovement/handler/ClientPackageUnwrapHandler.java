package com.molox.cimprovement.handler;

import com.molox.cimprovement.network.UnwrapPackagePacket;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ClientPackageUnwrapHandler {

    @SubscribeEvent
    public void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null) return;

        ItemStack stack = hoveredSlot.getItem();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof PackageItem)) return;

        PacketDistributor.sendToServer(new UnwrapPackagePacket(hoveredSlot.index));
        event.setCanceled(true);
    }
}