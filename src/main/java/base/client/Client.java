package base.client;

import base.client.cmd.CommandManager;
import base.client.effekseer.installer.LoadNatives;
import base.client.event.EventManager;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventShutdownClient;
import base.client.event.events.impl.input.EventKeyPress;
import base.client.feature.Module;
import base.client.files.FileManager;
import base.client.files.impl.FriendConfig;
import base.client.files.impl.MacroConfig;
import base.client.files.impl.MainConfig;
import base.client.gui.click.ClickGuiScreen;
import base.client.helpers.Helper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.impl.render.render3d.RenderPresets;
import base.client.macro.Macro;
import base.client.managers.*;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Client implements Helper {

    public static Client instance = new Client();
    public static boolean isghost = false;
    public static final String name = "Sprite";
    public static String clientName = "Sprite";
    public String type = "Fabric";
    public String version = "1.0";
    public String status = "Release";
    public String configname = "StartConfig";
    public String langname = "en";
    public Queue<String> devs = new LinkedList<>();

    // clientname - can be renamed

    public CommandManager commandManager;
    public ModuleManager featureManager = new ModuleManager();
    public MacroManager macroManager;
    public PacketHelper.Values packet = new PacketHelper.Values();
    public SoundManager soundManager;
    public FileManager fileManager;
    public FriendManager friendManager;
    public ConfigManager configManager;
    public LangManager langManager;
    public FlagHelper.Flags flagsch = new FlagHelper.Flags();

    public ClickGuiScreen clickGuiScreen;

    public static LoadNatives loadNatives;

    public static float timerspeed = 1f;

    public static boolean running = false;

    public static Map<String, String> langmap = new HashMap<>();

    public void load() throws IOException {
        configname = "StartConfig";

        // initialize early stuff that doesn't need auth
        RenderPresets.init();
        loadNatives = new LoadNatives();
        fileManager = new FileManager();
        configManager = new ConfigManager();
        friendManager = new FriendManager();
        langManager = new LangManager();
        TranslationManager.init();
        soundManager = new SoundManager();
        featureManager = new ModuleManager();
        macroManager = new MacroManager();
        commandManager = new CommandManager();
        packet = new PacketHelper.Values();
        flagsch = new FlagHelper.Flags();

        // register events early so auth tick handler works
        EventManager.register(packet);
        EventManager.register(this);

        // no-auth mode: skip authentication to fully expose client
        finishInitialization();
    }

    private void finishInitialization() throws IOException {
        fileManager.loadFiles();

        try {
            fileManager.getFile(FriendConfig.class).loadFile();
        } catch (Exception ignored) {
        }

        try {
            fileManager.getFile(MacroConfig.class).loadFile();
        } catch (Exception ignored) {
        }

        try {
            fileManager.getFile(MainConfig.class).loadFile();
        } catch (Exception ignored) {
        }

        // load hud state config AFTER main config, to restore HUD module states
        try {
            fileManager.getFile(base.client.files.impl.HudStateConfig.class).loadFile();
        } catch (Exception ignored) {
        }

        soundManager.registerSounds();

        running = true;

        clickGuiScreen = new ClickGuiScreen(featureManager);
    }

    public static String translate(String label) {
        return langmap.get(label) == null ? label : langmap.get(label);
    }

    public static void clear() {
        langmap.clear();
    }

    public static LoadNatives getNativeLoader() {
        return loadNatives;
    }

    @EventTarget
    public void shutDown(EventShutdownClient event) {
        EventManager.unregister(this);
        Client.instance.configManager.saveConfig(Client.instance.configname);
        // Client.instance.langManager.saveLang(Client.instance.langname); // ForTesting
        (fileManager = new FileManager()).saveFiles();
    }

    @EventTarget
    public void onInputKey(EventKeyPress event) {
        // if (event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
        // mc.setScreen(clickGuiScreen);
        // }

        for (Module feature : featureManager.getModuleList()) {
            if (feature.getBind() == event.getKey()) {

                feature.toggle();
            }
        }
        for (Macro macro : macroManager.getMacros()) {
            if (macro.getKey() == event.getKey()) {
                if (mc.player != null && mc.player.getHealth() > 0) {
                    ChatHelper.addChatMessage(macro.getValue());
                }
            }
        }
    }
}
