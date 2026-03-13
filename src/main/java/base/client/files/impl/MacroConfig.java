package base.client.files.impl;



import base.client.Client;
import base.client.files.FileManager;
import base.client.helpers.utils.KeyUtil;
import base.client.macro.Macro;

import java.io.*;

public class MacroConfig extends FileManager.CustomFile {

    public MacroConfig(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

    public void loadFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(this.getFile().getAbsolutePath());
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                String curLine = line.trim();
                String bind = curLine.split(":")[0];
                String value = curLine.split(":")[1];
                if (Client.instance.macroManager != null) {
                    Client.instance.macroManager.addMacro(new Macro(KeyUtil.StringToKey(bind).getValue(), value));
                }
            }
            br.close();
        } catch (Exception e) {

        }
    }

    public void saveFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(this.getFile()));
            for (Macro m : Client.instance.macroManager.getMacros()) {
                if (m != null) {
                    out.write(KeyUtil.KeyToString(m.getKey()) + ":" + m.getValue());
                    out.write("\r\n");
                }
            }
            out.close();
        } catch (Exception ignored) {

        }
    }
}
