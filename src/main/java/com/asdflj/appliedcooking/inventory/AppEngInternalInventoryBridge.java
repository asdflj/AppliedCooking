package com.asdflj.appliedcooking.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.appliedcooking.util.WirelessObject;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

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
    public void readFromNBT(final NBTTagCompound data, final String name) {

    }

    @Override
    public void readFromNBT(final NBTTagCompound target) {

    }

    public void push(int i, ItemStack itemStack) {
        this.inv[i] = itemStack;
    }
}
