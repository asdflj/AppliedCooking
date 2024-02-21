package com.asdflj.appliedcooking.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.asdflj.appliedcooking.AppliedCooking;
import com.asdflj.appliedcooking.common.tile.TileKitchenStation;
import com.asdflj.appliedcooking.loader.ItemAndBlockHolder;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ItemKitchenStationRender implements IItemRenderer {

    public final ResourceLocation keyboard = new ResourceLocation(
        AppliedCooking.MODID,
        "textures/kitchen_station_keyboard.png");
    public final ResourceLocation screen = new ResourceLocation(
        AppliedCooking.MODID,
        "textures/kitchen_station_screen.png");

    public static IModelCustom modelKitchenStation = AdvancedModelLoader
        .loadModel(AppliedCooking.resource("models/kitchen_station.obj"));

    public ItemKitchenStationRender() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileKitchenStation.class, new RenderBlockKitchenStation());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ItemAndBlockHolder.KITCHEN_STATION), this);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        GL11.glScalef(1.7f, 1.7f, 1.7f);
        GL11.glRotated(180, 0, 1, 0);
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(-0.2f, 0.8f, -0.4f);
                GL11.glRotated(-90, 0, 1, 0);
                break;
            case INVENTORY:
                GL11.glTranslatef(0F, 0.3F, 0F);
                break;
            default:
                GL11.glTranslatef(0, 0.7F, -0.5F);
                break;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(screen);
        modelKitchenStation.renderPart("screen");
        Minecraft.getMinecraft().renderEngine.bindTexture(keyboard);
        modelKitchenStation.renderPart("keyboard");
        modelKitchenStation.renderPart("pillar");
        modelKitchenStation.renderPart("power_button");
        GL11.glPopMatrix();
    }
}
