package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventCollideEntity;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.motion.EventPushOutOfBlocks;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class FreeCam extends Module {

    public NumberSetting verspeed = new NumberSetting("Vertical Speed", 0.5F, 0.01F, 5F, 0.01F, () -> true);
    public NumberSetting horspeed = new NumberSetting("Horizontal Speed", 0.5F, 0.01F, 5F, 0.01F, () -> true);

    public BooleanSetting AntiAction = new BooleanSetting("AntiAction", true, () -> true);
    public BooleanSetting AntiTp = new BooleanSetting("AntiTp", true, () -> true);
    public BooleanSetting autoDamageDisable = new BooleanSetting("Auto Damage Disable", false, () -> true);

    public BooleanSetting SaveMotion = new BooleanSetting("Save Motion", true, () -> true);
    private float old;
    private double oldX;
    private double oldY;
    private double oldZ;

    double motX;
    double motY;
    double motZ;

    public FreeCam() {
        super("FreeCam", "Позволяет летать в свободной камере", Type.Player);
        addSettings(horspeed, verspeed, SaveMotion, AntiAction, AntiTp, autoDamageDisable);
    }

    @Override
    public void onDisable() {
        MoveUtil.NoCliping = false;

        if (SaveMotion.isEnabled()) {
            MoveUtil.setmotXYZ(motX, motY, motZ);
        }
        mc.player.setPos(oldX, oldY, oldZ);
        // mc.level.removeEntity(-69, Entity.RemovalReason.CHANGED_DIMENSION);
        super.onDisable();
    }

    @Override
    public void onEnable() {
        motX = mc.player.getDeltaMovement().x();
        motY = mc.player.getDeltaMovement().y();
        motZ = mc.player.getDeltaMovement().z();

        UUID uuid = UUID.nameUUIDFromBytes(
                ("FakePlayer_" + mc.player.blockPosition().toString()).getBytes());

        // note: GameProfile is now a record in 1.21.11, properties are immutable
        // use player's profile directly or create without property copying
        GameProfile profile = new GameProfile(uuid, mc.getGameProfile().name());

        /*
         * RemotePlayer fakePlayer2 = new RemotePlayer(mc.level,profile);
         * fakePlayer2.copyPosition(mc.player);
         * fakePlayer2.forceSetRotation(mc.player.getYRot(),mc.player.getXRot());
         * fakePlayer2.setId(-69);
         * 
         * mc.level.addEntity(fakePlayer2);
         */

        oldX = mc.player.getX();
        oldY = mc.player.getY();
        oldZ = mc.player.getZ();

        super.onEnable();
    }

    /*
     * @EventTarget
     * public void onFullCube(EventFullCube event) {
     * event.setCancelled(true);
     * }
     * 
     * 
     * 
     */
    @EventTarget
    public void onCollide(EventCollideEntity event) {
        event.cancel();
    }

    @EventTarget
    public void onPush(EventPushOutOfBlocks event) {
        event.cancel();
    }

    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        PacketHelper.Values pc = Client.instance.packet;

        if (MoveUtil.motYstate() != 0) {
            MoveUtil.setmotY(MoveUtil.motYstate() > 0 ? verspeed.getValue() : -verspeed.getValue());
        } else {
            MoveUtil.setmotY(0);
        }
        MoveUtil.ssmartstrafe(horspeed.getValue());

    }

    @EventTarget
    public void ontick(EventTick event) {
        if (autoDamageDisable.isEnabled() && mc.player.hurtTime > 0
                && Client.instance.featureManager.getModuleByClass(FreeCam.class).getState()) {
            MoveUtil.NoCliping = false;
            this.toggle();
        }
        mc.player.noPhysics = true;
        MoveUtil.NoCliping = true;

    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {

        mc.player.noPhysics = true;

    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        if (AntiTp.isEnabled() && mc.player != null) {
            if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacketCancel event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
            event.setCancelled(true);
        }
        if (AntiAction.isEnabled()) {

            if (event.getPacket() instanceof ServerboundPlayerCommandPacket) {
                event.setCancelled(true);
            }
        }
    }

}
