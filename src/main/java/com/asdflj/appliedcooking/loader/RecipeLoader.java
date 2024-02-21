package com.asdflj.appliedcooking.loader;

import static com.asdflj.appliedcooking.loader.ItemAndBlockHolder.KITCHEN_STATION;
import static net.blay09.mods.cookingforblockheads.CookingForBlockheads.*;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();
    public static final ItemStack TOASTER = new ItemStack(blockToaster, 1);
    public static final ItemStack OVEN = new ItemStack(blockOven, 1);
    public static final ItemStack BOOK_TIER1 = new ItemStack(itemRecipeBook, 1);
    public static final ItemStack BOOK_TIER2 = new ItemStack(itemRecipeBook, 1, 1);
    public static final ItemStack AE2_ME_CHEST = GameRegistry
        .findItemStack("appliedenergistics2", "tile.BlockChest", 1);

    public static final ItemStack AE2_GLASS_CABLE = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
        1,
        16);
    public static final ItemStack AE2_PROCESS_ENG = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
        1,
        24);
    public static final ItemStack AE2_CELL_16K = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
        1,
        37);

    @Override
    public void run() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                KITCHEN_STATION.stack(),
                "TCO",
                "GKG",
                "BPN",
                'T',
                TOASTER,
                'C',
                AE2_ME_CHEST,
                'O',
                OVEN,
                'G',
                AE2_GLASS_CABLE,
                'K',
                AE2_CELL_16K,
                'B',
                BOOK_TIER1,
                'P',
                AE2_PROCESS_ENG,
                'N',
                BOOK_TIER2));
    }
}
