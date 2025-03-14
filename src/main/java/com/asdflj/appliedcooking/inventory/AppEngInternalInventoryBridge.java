package com.asdflj.appliedcooking.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.appliedcooking.util.WirelessObject;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.item.AEItemStack;

public class AppEngInternalInventoryBridge extends AppEngInternalInventory {

    private final WirelessObject obj;

    public AppEngInternalInventoryBridge(WirelessObject obj) {
        super(
            obj,
            obj.getStorageList()
                .size(),
            Integer.MAX_VALUE,
            true);
        this.obj = obj;
    }

    @Override
    public void setInventorySlotContents(final int slot, final ItemStack newItemStack) {
        final ItemStack oldStack = this.inv[slot];
        this.inv[slot] = newItemStack;
        if (this.obj != null && this.eventsEnabled()) {
            this.obj.onChangeInventory(this, slot, InvOperation.setInventorySlotContents, newItemStack, oldStack);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (this.obj.getStorageList() == null) {
            return null;
        }
        IAEItemStack item = AEItemStack.create(super.getStackInSlot(slot));
        if (item == null) {
            return null;
        }
        IAEItemStack stored = this.obj.getItemStorage()
            .getStorageList()
            .findPrecise(item);
        if (stored == null) {
            return null;
        }
        return stored.getItemStack();
    }

    @Override
    public void readFromNBT(final NBTTagCompound data, final String name) {

    }

    @Override
    public void readFromNBT(final NBTTagCompound target) {

    }

    public void push(int i, ItemStack itemStack) {
        this.inv[i] = itemStack;
    }
}
