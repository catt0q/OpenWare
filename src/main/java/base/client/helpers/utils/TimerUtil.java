package base.client.helpers.utils;

import base.client.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.TickRateManager;

public class TimerUtil {

    static TickRateManager tm= Minecraft.getInstance().level.tickRateManager();


    public static void setTickRate(float tickRate){
        Client.timerspeed=tickRate/20;
    }
    public static float getTimerspeed(){ return Client.timerspeed; }
    public static void reset(){ setTimerspeed(1F); }
    public static void setTimerspeed(double timerspeed){ setTimerspeed((float)timerspeed); }
    public static void setTimerspeed(float timerspeed){  if(timerspeed<0.01F){   return;    }   Client.timerspeed=timerspeed;   }

}
