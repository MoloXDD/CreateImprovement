package com.molox.cimprovement.handler;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class PackageUnwrapHandler {

    public static boolean unwrapPackage(ServerPlayer player, ItemStack packageStack, int slotIndex) {
        if (!(packageStack.getItem() instanceof PackageItem)) return false;

        ItemStackHandler contents = PackageItem.getContents(packageStack);
        if (contents == null) return false;

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack stack = contents.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }

        consumePackage(player, slotIndex);

        if (player.level() instanceof ServerLevel serverLevel) {
            AllSoundEvents.PACKAGE_POP.playOnServer(serverLevel, player.blockPosition());
        }

        if (items.isEmpty()) return true;

        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> slots = menu.slots;

        boolean packageInPlayerInv = slotIndex < slots.size()
                && slots.get(slotIndex) != null
                && slots.get(slotIndex).container instanceof Inventory;

        for (ItemStack item : items) {
            ItemStack remaining = item.copy();

            if (packageInPlayerInv) {
                remaining = insertIntoSlots(slots, remaining, true, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, true, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, false, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, false, false);
            } else {
                remaining = insertIntoSlots(slots, remaining, false, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, false, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, true, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoSlots(slots, remaining, true, false);
            }

            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }

        return true;
    }

    private static void consumePackage(ServerPlayer player, int slotIndex) {
        AbstractContainerMenu menu = player.containerMenu;
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;

        Slot slot = menu.slots.get(slotIndex);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        stack.shrink(1);
        slot.setChanged();
        menu.broadcastChanges();
    }

    private static ItemStack insertIntoSlots(List<Slot> slots, ItemStack toInsert,
                                             boolean playerInv, boolean mergeOnly) {
        for (Slot slot : slots) {
            boolean isPlayerSlot = slot.container instanceof Inventory;
            if (isPlayerSlot != playerInv) continue;
            if (!slot.mayPlace(toInsert)) continue;

            ItemStack inSlot = slot.getItem();

            if (mergeOnly) {
                if (inSlot.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(inSlot, toInsert)) continue;

                int maxSize = Math.min(slot.getMaxStackSize(inSlot), inSlot.getMaxStackSize());
                int canAdd = maxSize - inSlot.getCount();
                if (canAdd <= 0) continue;

                int transfer = Math.min(canAdd, toInsert.getCount());
                inSlot.grow(transfer);
                toInsert.shrink(transfer);
                slot.setChanged();
            } else {
                if (!inSlot.isEmpty()) continue;

                int maxSize = Math.min(slot.getMaxStackSize(toInsert), toInsert.getMaxStackSize());
                int transfer = Math.min(maxSize, toInsert.getCount());

                slot.set(toInsert.copyWithCount(transfer));
                toInsert.shrink(transfer);
                slot.setChanged();
            }

            if (toInsert.isEmpty()) return ItemStack.EMPTY;
        }

        return toInsert;
    }
}