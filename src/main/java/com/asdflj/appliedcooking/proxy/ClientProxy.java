package com.asdflj.appliedcooking.proxy;

import com.asdflj.appliedcooking.loader.RenderLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        (new RenderLoader()).run();
    }
}
