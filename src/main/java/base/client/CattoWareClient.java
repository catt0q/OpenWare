package base.client;

import base.client.effekseer.installer.LoadNatives;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class CattoWareClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        /*
         * if(Client.running) {
         * ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
         * 
         * Client.instance.getNativeLoader().init();
         * System.out.println("Effekseer initialized!");
         * });
         * }
         */
    }
}
