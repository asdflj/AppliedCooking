package com.asdflj.appliedcooking.mixins;

import java.util.List;

import net.blay09.mods.cookingforblockheads.KitchenMultiBlock;
import net.blay09.mods.cookingforblockheads.container.ContainerRecipeBook;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.registry.food.FoodRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.appliedcooking.inventory.AppEngInternalInventoryBridge;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

@Mixin(ContainerRecipeBook.class)
public abstract class MixinContainerRecipeBook extends Container {

    @Shadow(remap = false)
    private KitchenMultiBlock kitchenMultiBlock;

    @Inject(method = "trySmelt", at = @At("HEAD"), cancellable = true, remap = false)
    private void trySmelt(EntityPlayer player, FoodRecipe recipe, boolean isShiftDown, CallbackInfo ci) {
        if (!recipe.isSmeltingRecipe()) {
            return;
        }
        List<IInventory> sourceInventories = kitchenMultiBlock.getSourceInventories(player.inventory);
        for (int i = 0; i < sourceInventories.size(); i++) {
            if (sourceInventories.get(i) instanceof AppEngInternalInventoryBridge aeInv) {
                for (ItemStack ingredientStack : recipe.getCraftMatrix()
                    .get(0)
                    .getItemStacks()) {
                    IAEItemStack storedItem = aeInv.getWirelessObject()
                        .getStorageList()
                        .findPrecise(AEItemStack.create(ingredientStack));
                    if (storedItem != null) {
                        int count = isShiftDown
                            ? Math.min(storedItem.getItemStack().stackSize, ingredientStack.getMaxStackSize())
                            : 1;
                        IAEItemStack requestExtractItem = storedItem.copy();
                        requestExtractItem.setStackSize(count);
                        IAEItemStack result = aeInv.getWirelessObject()
                            .extractItems(
                                requestExtractItem,
                                Actionable.MODULATE,
                                aeInv.getWirelessObject()
                                    .getSource());
                        kitchenMultiBlock.smeltItem(result.getItemStack(), result.getItemStack().stackSize);
                        ci.cancel();
                        return;
                    }
                }
            } else {
                for (int j = 0; j < sourceInventories.get(i)
                    .getSizeInventory(); j++) {
                    ItemStack itemStack = sourceInventories.get(i)
                        .getStackInSlot(j);
                    if (itemStack != null) {
                        for (ItemStack ingredientStack : recipe.getCraftMatrix()
                            .get(0)
                            .getItemStacks()) {
                            if (CookingRegistry.areItemStacksEqualWithWildcard(itemStack, ingredientStack)) {
                                int count = isShiftDown
                                    ? Math.min(itemStack.stackSize, ingredientStack.getMaxStackSize())
                                    : 1;
                                ItemStack restStack = kitchenMultiBlock.smeltItem(itemStack, count);
                                sourceInventories.get(i)
                                    .setInventorySlotContents(j, restStack);
                                if (i == 0) { // Player Inventory
                                    if (j < 9) {
                                        ((EntityPlayerMP) player).sendSlotContents(this, 48 + j, restStack);
                                    } else {
                                        ((EntityPlayerMP) player).sendSlotContents(this, 21 + j - 9, restStack);
                                    }
                                }
                                player.inventory.markDirty();
                                ci.cancel();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
