package base.client.files.impl;



import base.client.Client;
import base.client.feature.impl.info.StaffDetector;
import base.client.files.FileManager;
import base.client.friend.Friend;

import java.io.*;


public class StaffListConfig extends FileManager.CustomFile {

    public StaffListConfig(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

    public void loadFile() throws IOException {
     
            BufferedReader br = new BufferedReader(new FileReader(this.getFile()));
            String line; StaffDetector.stafflist.clear();
            while ((line = br.readLine()) != null) {
                String curLine = line.trim();
                String name = curLine.split(":")[0];
                StaffDetector.stafflist.add(name);
            } 
            br.close();
   
    }

    public void saveFile() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(this.getFile()));
  
            for (String staff : StaffDetector.stafflist) {
                out.write(staff.replace(" ", ""));
                out.write("\r\n");
            }
            out.close();
     
    }
}
