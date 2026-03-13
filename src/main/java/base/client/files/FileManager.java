package base.client.files;

import base.client.Client;
import base.client.files.impl.FriendConfig;
import base.client.files.impl.HudStateConfig;
import base.client.files.impl.MacroConfig;
import base.client.files.impl.MainConfig;
import base.client.files.impl.StaffListConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FileManager {

    public static File directory = new File(Client.instance.name);
    public static ArrayList<CustomFile> files = new ArrayList<>();

    public FileManager() {
        files.add(new MainConfig("MainConfig", true));
        files.add(new FriendConfig("FriendConfig", true));
        files.add(new MacroConfig("MacroConfig", true));
        files.add(new StaffListConfig("StaffListConfig", true));
        files.add(new HudStateConfig("HudStateConfig", false)); // loaded manually after modules init
    }

    public void loadFiles() {
        for (CustomFile file : files) {
            try {
                if (file.loadOnStart()) {
                    file.loadFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFiles() {
        for (CustomFile f : files) {
            try {
                f.saveFile();
            } catch (Exception e) {

            }
        }
    }

    public CustomFile getFile(Class<?> clazz) {
        Iterator<CustomFile> customFileIterator = files.iterator();

        CustomFile file;
        do {
            if (!customFileIterator.hasNext()) {
                return null;
            }

            file = customFileIterator.next();
        } while (file.getClass() != clazz);

        return file;
    }

    public abstract static class CustomFile {

        private final File file;
        private final String name;
        private final boolean load;

        public CustomFile(String name, boolean loadOnStart) {
            this.name = name;
            this.load = loadOnStart;
            this.file = new File(FileManager.directory, name + ".json");
            if (!this.file.exists()) {
                try {
                    this.saveFile();
                } catch (Exception e) {

                }
            }
        }

        public final File getFile() {
            return this.file;
        }

        private boolean loadOnStart() {
            return this.load;
        }

        public final String getName() {
            return this.name;
        }

        public abstract void loadFile() throws Exception;

        public abstract void saveFile() throws Exception;
    }
}
