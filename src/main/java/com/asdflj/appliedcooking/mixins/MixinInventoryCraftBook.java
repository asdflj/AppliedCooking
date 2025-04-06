package com.asdflj.appliedcooking.mixins;

import java.util.Collection;
import java.util.List;

import net.blay09.mods.cookingforblockheads.api.kitchen.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.container.ContainerRecipeBook;
import net.blay09.mods.cookingforblockheads.container.inventory.InventoryCraftBook;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.blay09.mods.cookingforblockheads.registry.food.FoodIngredient;
import net.blay09.mods.cookingforblockheads.registry.food.FoodRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.appliedcooking.inventory.AppEngInternalInventoryBridge;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;

@Mixin(InventoryCraftBook.class)
public abstract class MixinInventoryCraftBook extends InventoryCrafting {

    @Shadow(remap = false)
    private List<IInventory> inventories;

    @Shadow(remap = false)
    @Final
    private int[] sourceInventories;

    @Shadow(remap = false)
    @Final
    private int[] sourceInventorySlots;

    @Shadow(remap = false)
    @Final
    private List<IKitchenItemProvider> sourceProviders;

    @Shadow(remap = false)
    private List<IKitchenItemProvider> itemProviders;

    @Shadow(remap = false)
    private IRecipe currentRecipe;

    @Shadow(remap = false)
    public abstract IRecipe prepareRecipe(EntityPlayer player, FoodRecipe recipe);

    @Shadow(remap = false)
    @Final
    private ContainerRecipeBook containerRecipeBook;

    public MixinInventoryCraftBook(Container p_i1807_1_, int p_i1807_2_, int p_i1807_3_) {
        super(p_i1807_1_, p_i1807_2_, p_i1807_3_);
    }

    @Inject(method = "prepareRecipe", at = @At("HEAD"), remap = false, cancellable = true)
    public void _prepareRecipe(EntityPlayer player, FoodRecipe recipe, CallbackInfoReturnable<IRecipe> cir) {
        List<FoodIngredient> ingredients = recipe.getCraftMatrix();
        IItemList<IAEItemStack> usedStacks = AEApi.instance()
            .storage()
            .createItemList();
        int[][] usedStackSize = new int[inventories.size()][];
        for (int i = 0; i < usedStackSize.length; i++) {
            usedStackSize[i] = new int[inventories.get(i)
                .getSizeInventory()];
        }
        for (int i = 0; i < getSizeInventory(); i++) {
            setInventorySlotContents(i, null);
            sourceInventories[i] = -1;
            sourceInventorySlots[i] = -1;
        }
        ingredientLoop: for (int i = 0; i < ingredients.size(); i++) {
            int origX = i % recipe.getRecipeWidth();
            int origY = i / recipe.getRecipeWidth();
            int targetIdx = origY * 3 + origX;

            if (ingredients.get(i) != null) {
                sourceProviders.clear();
                for (IKitchenItemProvider itemProvider : itemProviders) {
                    itemProvider.clearCraftingBuffer();
                    for (ItemStack providedStack : itemProvider.getProvidedItemStacks()) {
                        if (ingredients.get(i)
                            .isValidItem(providedStack)) {
                            ItemStack itemStack = providedStack.copy();
                            if (itemProvider.addToCraftingBuffer(itemStack)) {
                                sourceProviders.add(itemProvider);
                                setInventorySlotContents(targetIdx, itemStack);
                                continue ingredientLoop;
                            }
                        }
                    }
                }
                for (int j = 0; j < inventories.size(); j++) {
                    if (inventories.get(j) instanceof AppEngInternalInventoryBridge aeInv) {
                        for (ItemStack item : ingredients.get(i)
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
                                IAEItemStack extracted = aeInv.getWirelessObject()
                                    .getItemStorage()
                                    .extractItems(
                                        result,
                                        Actionable.MODULATE,
                                        aeInv.getWirelessObject()
                                            .getSource());
                                if (extracted == null) continue;
                                aeInv.getWirelessObject()
                                    .getItemStorage()
                                    .injectItems(
                                        extracted,
                                        Actionable.MODULATE,
                                        aeInv.getWirelessObject()
                                            .getSource());
                                if (ingredients.get(i)
                                    .isValidItem(extracted.getItemStack())) {
                                    extracted.setStackSize(1);
                                    usedStacks.add(extracted);
                                    if (checkHasEnoughItem(
                                        usedStacks,
                                        aeInv.getWirelessObject()
                                            .getStorageList())) {
                                        sourceInventories[targetIdx] = j;
                                        setInventorySlotContents(targetIdx, extracted.getItemStack());
                                        continue ingredientLoop;
                                    }
                                }

                            }
                        }
                    } else {
                        for (int k = 0; k < inventories.get(j)
                            .getSizeInventory(); k++) {
                            ItemStack itemStack = inventories.get(j)
                                .getStackInSlot(k);
                            if (itemStack != null && ingredients.get(i)
                                .isValidItem(itemStack) && itemStack.stackSize - usedStackSize[j][k] > 0) {
                                usedStackSize[j][k]++;
                                setInventorySlotContents(targetIdx, itemStack);
                                sourceInventories[targetIdx] = j;
                                sourceInventorySlots[targetIdx] = k;
                                continue ingredientLoop;
                            }
                        }
                    }

                }
            }
        }
        currentRecipe = CookingRegistry.findMatchingFoodRecipe((InventoryCraftBook) (Object) this, player.worldObj);
        cir.setReturnValue(currentRecipe);
    }

    private static boolean checkHasEnoughItem(IItemList<IAEItemStack> list, IItemList<IAEItemStack> storedItems) {
        for (IAEItemStack item : list) {
            IAEItemStack stored = storedItems.findPrecise(item);
            if (stored == null || stored.getStackSize() < item.getStackSize()) return false;
        }
        return true;
    }

    @Inject(method = "craft", at = @At("HEAD"), remap = false, cancellable = true)
    public void craft(EntityPlayer player, FoodRecipe recipe, CallbackInfoReturnable<ItemStack> cir) {
        prepareRecipe(player, recipe);
        if (currentRecipe == null) {
            // Recipe was not found. This is probably caused by missing tool.
            // In case the tool has just been depleted after initial recipe selection, update tooltip.
            containerRecipeBook.markSelectionDirty();
            cir.setReturnValue(null);
            return;
        }
        if (currentRecipe.matches(this, player.worldObj)) {
            ItemStack craftingResult = currentRecipe.getCraftingResult(this);
            if (craftingResult != null) {
                // Fire FML Events
                FMLCommonHandler.instance()
                    .firePlayerCraftingEvent(player, craftingResult, this);
                craftingResult.onCrafting(player.worldObj, player, 1);
                // Handle Vanilla Achievements
                if (craftingResult.getItem() == Items.bread) {
                    player.addStat(AchievementList.makeBread, 1);
                } else if (craftingResult.getItem() == Items.cake) {
                    player.addStat(AchievementList.bakeCake, 1);
                }
                // Kill ingredients
                for (int i = 0; i < getSizeInventory(); i++) {
                    ItemStack itemStack = getStackInSlot(i);
                    if (itemStack != null) {
                        if (!extractItemStackFromAE(itemStack)) decrStackSize(i, 1);
                        if (itemStack.getItem()
                            .hasContainerItem(itemStack) && sourceInventories[i] != -1) {
                            // Fire PlayerDestroyItem event
                            ItemStack containerItem = itemStack.getItem()
                                .getContainerItem(itemStack);
                            if (containerItem != null && containerItem.isItemStackDamageable()
                                && itemStack.getItemDamage() > itemStack.getMaxDamage()) {
                                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, containerItem));
                                continue;
                            }
                            // Put container item back into crafting grid or drop it
                            if (!itemStack.getItem()
                                .doesContainerItemLeaveCraftingGrid(itemStack)
                                || !player.inventory.addItemStackToInventory(containerItem)) {
                                if (getStackInSlot(i) == null) {
                                    setInventorySlotContents(i, containerItem);
                                } else if (!tryInjectContainerItemToAE(containerItem)) {
                                    player.dropPlayerItemWithRandomChoice(containerItem, false);
                                }
                            }
                        }

                        if (sourceInventories[i] != -1 && sourceInventorySlots[i] != -1) {
                            inventories.get(sourceInventories[i])
                                .setInventorySlotContents(sourceInventorySlots[i], this.getStackInSlot(i));
                        }
                    }
                }
                for (IKitchenItemProvider itemProvider : sourceProviders) {
                    itemProvider.craftingComplete();
                }
            }
            cir.setReturnValue(craftingResult);
            return;
        }
        cir.setReturnValue(null);
    }

    private boolean tryInjectContainerItemToAE(ItemStack what) {
        for (IInventory inv : inventories) {
            if (inv instanceof AppEngInternalInventoryBridge aeInv) {
                IAEItemStack injected = aeInv.getWirelessObject()
                    .injectItems(
                        AEItemStack.create(what),
                        Actionable.MODULATE,
                        aeInv.getWirelessObject()
                            .getSource());
                if (injected == null) return true;
            }
        }
        return false;
    }

    private boolean extractItemStackFromAE(ItemStack what) {
        for (IInventory inv : inventories) {
            if (inv instanceof AppEngInternalInventoryBridge aeInv) {
                ItemStack extractItem = what.copy();
                extractItem.stackSize = 1;
                IAEItemStack extracted = aeInv.getWirelessObject()
                    .extractItems(
                        AEItemStack.create(extractItem),
                        Actionable.MODULATE,
                        aeInv.getWirelessObject()
                            .getSource());
                if (extracted != null) return true;
            }
        }
        return false;
    }

}
