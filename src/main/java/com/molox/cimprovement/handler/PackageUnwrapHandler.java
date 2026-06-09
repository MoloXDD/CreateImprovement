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

    private enum SlotArea {
        HOTBAR,
        PLAYER_INV,
        CONTAINER
    }

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

        SlotArea packageArea = getSlotArea(slots, slotIndex);

        for (ItemStack item : items) {
            ItemStack remaining = item.copy();

            if (packageArea == SlotArea.HOTBAR) {
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, false);
            } else if (packageArea == SlotArea.PLAYER_INV) {
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, false);
            } else {
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.CONTAINER, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.PLAYER_INV, false);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, true);
                if (remaining.isEmpty()) continue;
                remaining = insertIntoArea(slots, remaining, SlotArea.HOTBAR, false);
            }

            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }

        return true;
    }

    private static SlotArea getSlotArea(List<Slot> slots, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return SlotArea.CONTAINER;
        Slot slot = slots.get(slotIndex);
        if (!(slot.container instanceof Inventory)) return SlotArea.CONTAINER;
        return Inventory.isHotbarSlot(slot.getContainerSlot()) ? SlotArea.HOTBAR : SlotArea.PLAYER_INV;
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

    private static ItemStack insertIntoArea(List<Slot> slots, ItemStack toInsert,
                                            SlotArea area, boolean mergeOnly) {
        for (Slot slot : slots) {
            if (getArea(slot) != area) continue;
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

    private static SlotArea getArea(Slot slot) {
        if (!(slot.container instanceof Inventory)) return SlotArea.CONTAINER;
        return Inventory.isHotbarSlot(slot.getContainerSlot()) ? SlotArea.HOTBAR : SlotArea.PLAYER_INV;
    }
}