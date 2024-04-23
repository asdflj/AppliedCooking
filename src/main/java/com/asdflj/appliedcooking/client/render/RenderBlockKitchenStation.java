package com.asdflj.appliedcooking.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.asdflj.appliedcooking.AppliedCooking;
import com.asdflj.appliedcooking.common.tile.TileKitchenStation;

public class RenderBlockKitchenStation extends TileEntitySpecialRenderer {

    public final ResourceLocation keyboard = new ResourceLocation(
        AppliedCooking.MODID,
        "textures/kitchen_station_keyboard.png");
    public final ResourceLocation screen = new ResourceLocation(
        AppliedCooking.MODID,
        "textures/kitchen_station_screen.png");
    IModelCustom modelKitchenStation = AdvancedModelLoader
        .loadModel(AppliedCooking.resource("models/kitchen_station.obj"));
    IModelCustom modelKitchenStationConnected = AdvancedModelLoader
        .loadModel(AppliedCooking.resource("models/kitchen_station_connected.obj"));

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5f, y + 0.6f, z + 0.5f);
        int orientation = tileentity.getBlockMetadata();
        if (orientation == 4) {
            GL11.glRotatef(90, 0, 1, 0);
        } else if (orientation == 5) {
            GL11.glRotatef(-90, 0, 1, 0);
        } else if (orientation == 3) {
            GL11.glRotatef(180, 0, 1, 0);
        }
        IModelCustom model;
        if (((TileKitchenStation) tileentity).isActive()) {
            model = modelKitchenStationConnected;
        } else {
            model = modelKitchenStation;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(screen);
        model.renderPart("screen");
        Minecraft.getMinecraft().renderEngine.bindTexture(keyboard);
        model.renderPart("keyboard");
        model.renderPart("pillar");
        model.renderPart("power_button");
        GL11.glPopMatrix();
    }
}
