package com.asdflj.appliedcooking.inventory;

import net.minecraft.inventory.IInventory;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.IAEAppEngInventory;

public interface IWirelessObject extends ITerminalHost, IMEMonitor<IAEItemStack>, IAEAppEngInventory, IActionHost {

    IInventory getInventory();
}
