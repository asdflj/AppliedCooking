package com.asdflj.appliedcooking.mixins;

import java.util.Collection;
import java.util.List;

import net.blay09.mods.cookingforblockheads.api.kitchen.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.registry.food.FoodIngredient;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.appliedcooking.inventory.AppEngInternalInventoryBridge;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

@Mixin(CookingRegistry.class)
public abstract class MixinCookingRegistry {

    @Inject(method = "areIngredientsAvailableFor", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onMatrixLoopCompletion(List<FoodIngredient> craftMatrix, List<IInventory> inventories,
        List<IKitchenItemProvider> itemProviders, CallbackInfoReturnable<Boolean> cir) {
        int[][] usedStackSize = new int[inventories.size()][];
        for (int i = 0; i < usedStackSize.length; i++) {
            usedStackSize[i] = new int[inventories.get(i)
                .getSizeInventory()];
        }
        boolean[] itemFound = new boolean[craftMatrix.size()];
        IItemList<IAEItemStack> usedStacks = AEApi.instance()
            .storage()
            .createItemList();

        matrixLoop: for (int i = 0; i < craftMatrix.size(); i++) {
            if (craftMatrix.get(i) == null || craftMatrix.get(i)
                .isToolItem()) {
                itemFound[i] = true;
                continue;
            }
            for (IKitchenItemProvider itemProvider : itemProviders) {
                itemProvider.clearCraftingBuffer();
                for (ItemStack providedStack : itemProvider.getProvidedItemStacks()) {
                    if (craftMatrix.get(i)
                        .isValidItem(providedStack)) {
                        if (itemProvider.addToCraftingBuffer(providedStack)) {
                            itemFound[i] = true;
                            continue matrixLoop;
                        }
                    }
                }
            }
            for (int j = 0; j < inventories.size(); j++) {
                if (inventories.get(j) instanceof AppEngInternalInventoryBridge aeInv) {
                    for (ItemStack item : craftMatrix.get(i)
                        .getItemStacks()) {
                        IAEItemStack requestItem = AEItemStack.create(item);
                        if (requestItem == null) continue;
                        IAEItemStack usedItem = usedStacks.findPrecise(requestItem);
                        if (usedItem != null) {
                            requestItem.add(usedItem);
                        }
                        Collection<IAEItemStack> results = aeInv.getWirelessObject()
                            .getStorageList()
                            .findFuzzy(requestItem, FuzzyMode.IGNORE_ALL);
                        for (IAEItemStack result : results) {
                            if (craftMatrix.get(i)
                                .isValidItem(result.getItemStack())) {
                                if (usedItem == null) {
                                    usedStacks.add(requestItem);
                                } else {
                                    usedStacks.findPrecise(requestItem)
                                        .incStackSize(1);
                                }
                                if (checkHasEnoughItem(
                                    usedStacks,
                                    aeInv.getWirelessObject()
                                        .getStorageList())) {
                                    itemFound[i] = true;
                                    continue matrixLoop;
                                }
                            }
                        }
                    }
                } else {
                    for (int k = 0; k < inventories.get(j)
                        .getSizeInventory(); k++) {
                        ItemStack itemStack = inventories.get(j)
                            .getStackInSlot(k);
                        if (itemStack != null && craftMatrix.get(i)
                            .isValidItem(itemStack) && itemStack.stackSize - usedStackSize[j][k] > 0) {
                            usedStackSize[j][k]++;
                            itemFound[i] = true;
                            continue matrixLoop;
                        }
                    }
                }

            }
        }
        for (int i = 0; i < itemFound.length; i++) {
            if (!itemFound[i]) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }

    private static boolean checkHasEnoughItem(IItemList<IAEItemStack> list, IItemList<IAEItemStack> storedItems) {
        for (IAEItemStack item : list) {
            IAEItemStack stored = storedItems.findPrecise(item);
            if (stored == null || stored.getStackSize() < item.getStackSize()) return false;
        }
        return true;
    }
}
