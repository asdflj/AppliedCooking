package com.asdflj.appliedcooking.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.appliedcooking.util.WirelessObject;

import appeng.tile.inventory.AppEngInternalInventory;

public class AppEngInternalInventoryBridge extends AppEngInternalInventory {

    private final WirelessObject obj;

    public AppEngInternalInventoryBridge(WirelessObject obj) {
        super(obj, 0, 64, true);
        this.obj = obj;
    }

    @Override
    public void setInventorySlotContents(final int slot, final ItemStack newItemStack) {}

    public WirelessObject getWirelessObject() {
        return this.obj;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data, final String name) {

    }

    @Override
    public void readFromNBT(final NBTTagCompound target) {

    }
}
