package base.client.feature.impl.client;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;

public class ClientSounds extends Module {

	public static ModeSetting Mode = new ModeSetting("Mode", "Bubble", () -> true,"Bubble",
			"Break","Piano","Augustus","Sigma","Heavy","Discord",
			"Airpods","Akrien","Alarm","Baloon","BaloonFast",
			"Basic","Vega","Frontiers","Nursultan","Ori",
			"Ori2","Speech","SpeechEcho","Sweep",
			"Tone","Vl","Wood","Mouse","Execution");
     
	@Override
	public void onDisable() {
	    super.onDisable();
	}

	@Override
	public void onEnable() {
	    super.onEnable();
	}

    public ClientSounds() {
        super("ClientSounds", "Звуки при включении функции и выключении", Type.Client);
        this.addSettings(Mode);
    }
}
