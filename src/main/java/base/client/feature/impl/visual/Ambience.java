package base.client.feature.impl.visual;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class Ambience extends Module {

    private final NumberSetting spinspeed,customtime;
    private final ModeSetting tmode,wmode;
    private long spin = 0;

    public Ambience() {
        super("Ambience", "Позволяет менять погоду", Type.Visuals);
        tmode = new ModeSetting("Time Mode", "Spin", () -> true, "Day", "Night", "Morning", "Sunset", "Spin", "Custom", "None");
        wmode = new ModeSetting("Weather Mode", "Sun", () -> true, "Sun", "Rain", "Thunder", "None");

        spinspeed = new NumberSetting("Spin Speed", 2, -10, 10, 1, () -> tmode.getCurrentMode().equals("Spin"));
        customtime = new NumberSetting("Custom Time", 1200, 0, 12000, 1000, () -> tmode.getCurrentMode().equals("Custom"));

        addSettings(tmode, spinspeed,customtime,wmode);
    }
    @Override
    public void onEnable() {

        super.onEnable();
    }

    @Override
    public void onDisable() {

        super.onDisable();
    }
    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {
      String md= tmode.getCurrentMode();
        String wm= wmode.getCurrentMode();
        this.setSuffix(md);
                switch(md){
                    case("Spin"):
                        mc.level.getLevelData().setDayTime(spin);
                        this.spin = (long) (spin + spinspeed.getValue() * 100);
                         break;
                    case("Day"):
                        mc.level.getLevelData().setDayTime(5000);
                        break;
                    case("Night"):
                        mc.level.getLevelData().setDayTime(17000);
                        break;
                    case("Morning"):
                        mc.level.getLevelData().setDayTime(0);
                        break;
                    case("Sunset"):
                        mc.level.getLevelData().setDayTime(13000);
                        break;
                    case("Custom"):
                        mc.level.getLevelData().setDayTime((long) customtime.getValue());
                        break;
      }




        switch(wm){
            case("Sun"):
             mc.level.setRainLevel(0);
                mc.level.setThunderLevel(0);
                break;
            case("Rain"):
                mc.level.setRainLevel(1);
                mc.level.setThunderLevel(0);
                break;
            case("Thunder"):
                mc.level.setRainLevel(1);
                mc.level.setThunderLevel(1);
                break;
        }



    }
}
