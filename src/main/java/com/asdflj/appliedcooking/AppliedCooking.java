package com.asdflj.appliedcooking;

import net.minecraft.util.ResourceLocation;

import com.asdflj.appliedcooking.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = AppliedCooking.MODID,
    version = AppliedCooking.VERSION,
    name = AppliedCooking.MODNAME,
    dependencies = "required-after:appliedenergistics2;required-after:cookingforblockheads")
public class AppliedCooking {

    public static final String MODID = "appliedcooking";
    public static final String VERSION = Tags.VERSION;
    public static final String MODNAME = "AppliedCooking";

    @SidedProxy(
        clientSide = "com.asdflj.appliedcooking.proxy.ClientProxy",
        serverSide = "com.asdflj.appliedcooking.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
