package base.client.files.impl;



import base.client.Client;
import base.client.files.FileManager;

import java.io.*;
public class MainConfig extends FileManager.CustomFile {
    public MainConfig(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

 
    
    public void loadFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(this.getFile().getAbsolutePath());
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            line=br.readLine(); if(line==null) return;
            Client.instance.configname=line.trim();
            Client.instance.configManager.loadConfig(Client.instance.configname);
            line=br.readLine(); if(line==null) return;
            Client.instance.langname=line.trim();
            Client.instance.langManager.loadLang(Client.instance.langname);

       /*     
            line=br.readLine(); if(line==null) return;  String[] arguments=line.split(":");
            if (arguments.length > 2) { 	Alt altt=new Alt(arguments[0], arguments[1], arguments[2], arguments.length > 3 ? Alt.Status.valueOf(arguments[3]) : Alt.Status.Unchecked);     (GuiAltManager.loginThread = new AltLoginThread(altt)).start();    
            } else {  	Alt altt=new Alt(arguments[0], arguments[1]);   AltManager.lastAlt=altt;     (GuiAltManager.loginThread = new AltLoginThread(altt)).start();           }
             */

            
            
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(this.getFile()));
            out.write(Client.instance.configname);
            out.write("\r\n");
            out.write(Client.instance.langname);
            out.write("\r\n");
            out.write(Client.instance.clientName);
            out.write("\r\n");
      /*      if(AltManager.lastAlt!=null) {
                out.write(AltManager.lastAlt.getMask().equals("") ? (AltManager.lastAlt.getUsername() + ":" + AltManager.lastAlt.getPassword() + ":" + AltManager.lastAlt.getUsername() + ":" + AltManager.lastAlt.getStatus()) :
                	(AltManager.lastAlt.getUsername() + ":" + AltManager.lastAlt.getPassword() + ":" + AltManager.lastAlt.getMask() + ":" + AltManager.lastAlt.getStatus())); 
            }else {	  out.write(Minecraft.getInstance().getSession().getUsername() + "::" + Minecraft.getInstance().getSession().getUsername() + ":" + "Unchecked");  }
            out.write("\r\n"); */
            

       
            
            
            
            
            out.close();
        } catch (Exception ignored) {

        }
    }
    
    
}
