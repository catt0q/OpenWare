package base.client.files.impl;



import base.client.Client;
import base.client.files.FileManager;
import base.client.friend.Friend;

import java.io.*;


public class FriendConfig extends FileManager.CustomFile {

    public FriendConfig(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

    public void loadFile() throws IOException {
     
            BufferedReader br = new BufferedReader(new FileReader(this.getFile()));
            String line; Client.instance.friendManager.getFriends().clear();
            while ((line = br.readLine()) != null) {
                String curLine = line.trim();
                String name = curLine.split(":")[0];
                Client.instance.friendManager.addFriend(name);
            } 
            br.close();
   
    }

    public void saveFile() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(this.getFile()));
  
            for (Friend friend : Client.instance.friendManager.getFriends()) {
                out.write(friend.getName().replace(" ", ""));
                out.write("\r\n");
            }
            out.close();
     
    }
}
