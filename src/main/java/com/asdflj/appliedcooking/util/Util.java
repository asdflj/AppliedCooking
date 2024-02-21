package com.asdflj.appliedcooking.util;

import net.minecraft.inventory.IInventory;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.tile.inventory.AppEngInternalInventory;

public class Util {

    public static IInventory EmptyInventory = new AppEngInternalInventory(null, 0);

    public static ILocatable getSecurityStation(long key) {
        return AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(key);
    }
}
