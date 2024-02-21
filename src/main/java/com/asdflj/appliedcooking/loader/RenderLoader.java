package com.asdflj.appliedcooking.loader;

import com.asdflj.appliedcooking.client.render.ItemKitchenStationRender;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        new ItemKitchenStationRender();
    }
}
