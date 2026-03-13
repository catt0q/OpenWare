package base;

import base.client.Client;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CattoWare implements ModInitializer {
	@Override
	public void onInitialize() {

		try {

			Client.instance.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
