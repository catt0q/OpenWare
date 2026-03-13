package base.client.managers;

import base.client.feature.impl.client.ClientSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundManager {

    public final Identifier dairpods_s = Identifier.parse("quantum:dairpods");
    public SoundEvent dairpods_se = SoundEvent.createVariableRangeEvent(dairpods_s);

    public final Identifier eairpods_s = Identifier.parse("quantum:eairpods");
    public SoundEvent eairpods_se = SoundEvent.createVariableRangeEvent(eairpods_s);

    public final Identifier dakrien_s = Identifier.parse("quantum:dakrien");
    public SoundEvent dakrien_se = SoundEvent.createVariableRangeEvent(dakrien_s);

    public final Identifier eakrien_s = Identifier.parse("quantum:eakrien");
    public SoundEvent eakrien_se = SoundEvent.createVariableRangeEvent(eakrien_s);

    public final Identifier dalarm_s = Identifier.parse("quantum:dalarm");
    public SoundEvent dalarm_se = SoundEvent.createVariableRangeEvent(dalarm_s);

    public final Identifier ealarm_s = Identifier.parse("quantum:ealarm");
    public SoundEvent ealarm_se = SoundEvent.createVariableRangeEvent(ealarm_s);

    public final Identifier daugustus_s = Identifier.parse("quantum:daugustus");
    public SoundEvent daugustus_se = SoundEvent.createVariableRangeEvent(daugustus_s);

    public final Identifier eaugustus_s = Identifier.parse("quantum:eaugustus");
    public SoundEvent eaugustus_se = SoundEvent.createVariableRangeEvent(eaugustus_s);

    public final Identifier dbaloon_s = Identifier.parse("quantum:dbaloon");
    public SoundEvent dbaloon_se = SoundEvent.createVariableRangeEvent(dbaloon_s);

    public final Identifier ebaloon_s = Identifier.parse("quantum:ebaloon");
    public SoundEvent ebaloon_se = SoundEvent.createVariableRangeEvent(ebaloon_s);

    public final Identifier dbaloonfast_s = Identifier.parse("quantum:dbaloonfast");
    public SoundEvent dbaloonfast_se = SoundEvent.createVariableRangeEvent(dbaloonfast_s);

    public final Identifier ebaloonfast_s = Identifier.parse("quantum:ebaloonfast");
    public SoundEvent ebaloonfast_se = SoundEvent.createVariableRangeEvent(ebaloonfast_s);

    public final Identifier dbasic_s = Identifier.parse("quantum:dbasic");
    public SoundEvent dbasic_se = SoundEvent.createVariableRangeEvent(dbasic_s);

    public final Identifier ebasic_s = Identifier.parse("quantum:ebasic");
    public SoundEvent ebasic_se = SoundEvent.createVariableRangeEvent(ebasic_s);

    public final Identifier dbreak_s = Identifier.parse("quantum:dbreak");
    public SoundEvent dbreak_se = SoundEvent.createVariableRangeEvent(dbreak_s);

    public final Identifier ebreak_s = Identifier.parse("quantum:ebreak");
    public SoundEvent ebreak_se = SoundEvent.createVariableRangeEvent(ebreak_s);

    public final Identifier dbubble_s = Identifier.parse("quantum:dbubble");
    public SoundEvent dbubble_se = SoundEvent.createVariableRangeEvent(dbubble_s);

    public final Identifier ebubble_s = Identifier.parse("quantum:ebubble");
    public SoundEvent ebubble_se = SoundEvent.createVariableRangeEvent(ebubble_s);

    public final Identifier ddiscord_s = Identifier.parse("quantum:ddiscord");
    public SoundEvent ddiscord_se = SoundEvent.createVariableRangeEvent(ddiscord_s);

    public final Identifier ediscord_s = Identifier.parse("quantum:ediscord");
    public SoundEvent ediscord_se = SoundEvent.createVariableRangeEvent(ediscord_s);

    public final Identifier dfrontiers_s = Identifier.parse("quantum:dfrontiers");
    public SoundEvent dfrontiers_se = SoundEvent.createVariableRangeEvent(dfrontiers_s);

    public final Identifier efrontiers_s = Identifier.parse("quantum:efrontiers");
    public SoundEvent efrontiers_se = SoundEvent.createVariableRangeEvent(efrontiers_s);

    public final Identifier dheavy_s = Identifier.parse("quantum:dheavy");
    public SoundEvent dheavy_se = SoundEvent.createVariableRangeEvent(dheavy_s);

    public final Identifier eheavy_s = Identifier.parse("quantum:eheavy");
    public SoundEvent eheavy_se = SoundEvent.createVariableRangeEvent(eheavy_s);

    public final Identifier disable_s = Identifier.parse("quantum:disable");
    public SoundEvent disable_se = SoundEvent.createVariableRangeEvent(disable_s);

    public final Identifier enable_s = Identifier.parse("quantum:enable");
    public SoundEvent enable_se = SoundEvent.createVariableRangeEvent(enable_s);

    public final Identifier dnursultan_s = Identifier.parse("quantum:dnursultan");
    public SoundEvent dnursultan_se = SoundEvent.createVariableRangeEvent(dnursultan_s);

    public final Identifier enursultan_s = Identifier.parse("quantum:enursultan");
    public SoundEvent enursultan_se = SoundEvent.createVariableRangeEvent(enursultan_s);

    public final Identifier dori_s = Identifier.parse("quantum:dori");
    public SoundEvent dori_se = SoundEvent.createVariableRangeEvent(dori_s);

    public final Identifier eori_s = Identifier.parse("quantum:eori");
    public SoundEvent eori_se = SoundEvent.createVariableRangeEvent(eori_s);

    public final Identifier dori2_s = Identifier.parse("quantum:dori2");
    public SoundEvent dori2_se = SoundEvent.createVariableRangeEvent(dori2_s);

    public final Identifier eori2_s = Identifier.parse("quantum:eori2");
    public SoundEvent eori2_se = SoundEvent.createVariableRangeEvent(eori2_s);

    public final Identifier dpiano_s = Identifier.parse("quantum:dpiano");
    public SoundEvent dpiano_se = SoundEvent.createVariableRangeEvent(dpiano_s);

    public final Identifier epiano_s = Identifier.parse("quantum:epiano");
    public SoundEvent epiano_se = SoundEvent.createVariableRangeEvent(epiano_s);

    public final Identifier dsigma_s = Identifier.parse("quantum:dsigma");
    public SoundEvent dsigma_se = SoundEvent.createVariableRangeEvent(dsigma_s);

    public final Identifier esigma_s = Identifier.parse("quantum:esigma");
    public SoundEvent esigma_se = SoundEvent.createVariableRangeEvent(esigma_s);

    public final Identifier dspeech_s = Identifier.parse("quantum:dspeech");
    public SoundEvent dspeech_se = SoundEvent.createVariableRangeEvent(dspeech_s);

    public final Identifier espeech_s = Identifier.parse("quantum:espeech");
    public SoundEvent espeech_se = SoundEvent.createVariableRangeEvent(espeech_s);

    public final Identifier dspeechecho_s = Identifier.parse("quantum:dspeechecho");
    public SoundEvent dspeechecho_se = SoundEvent.createVariableRangeEvent(dspeechecho_s);

    public final Identifier espeechecho_s = Identifier.parse("quantum:espeechecho");
    public SoundEvent espeechecho_se = SoundEvent.createVariableRangeEvent(espeechecho_s);

    public final Identifier dsweep_s = Identifier.parse("quantum:dsweep");
    public SoundEvent dsweep_se = SoundEvent.createVariableRangeEvent(dsweep_s);

    public final Identifier esweep_s = Identifier.parse("quantum:esweep");
    public SoundEvent esweep_se = SoundEvent.createVariableRangeEvent(esweep_s);

    public final Identifier dtone_s = Identifier.parse("quantum:dtone");
    public SoundEvent dtone_se = SoundEvent.createVariableRangeEvent(dtone_s);

    public final Identifier etone_s = Identifier.parse("quantum:etone");
    public SoundEvent etone_se = SoundEvent.createVariableRangeEvent(etone_s);

    public final Identifier dvega_s = Identifier.parse("quantum:dvega");
    public SoundEvent dvega_se = SoundEvent.createVariableRangeEvent(dvega_s);

    public final Identifier evega_s = Identifier.parse("quantum:evega");
    public SoundEvent evega_se = SoundEvent.createVariableRangeEvent(evega_s);

    public final Identifier dvl_s = Identifier.parse("quantum:dvl");
    public SoundEvent dvl_se = SoundEvent.createVariableRangeEvent(dvl_s);

    public final Identifier evl_s = Identifier.parse("quantum:evl");
    public SoundEvent evl_se = SoundEvent.createVariableRangeEvent(evl_s);

    public final Identifier dwood_s = Identifier.parse("quantum:dwood");
    public SoundEvent dwood_se = SoundEvent.createVariableRangeEvent(dwood_s);

    public final Identifier ewood_s = Identifier.parse("quantum:ewood");
    public SoundEvent ewood_se = SoundEvent.createVariableRangeEvent(ewood_s);

    public final Identifier dmouse_s = Identifier.parse("quantum:dmouse");
    public SoundEvent dmouse_se = SoundEvent.createVariableRangeEvent(dmouse_s);

    public final Identifier emouse_s = Identifier.parse("quantum:emouse");
    public SoundEvent emouse_se = SoundEvent.createVariableRangeEvent(emouse_s);

    public final Identifier dexecution_s = Identifier.parse("quantum:dexecution");
    public SoundEvent dexecution_se = SoundEvent.createVariableRangeEvent(dexecution_s);

    public final Identifier eexecution_s = Identifier.parse("quantum:eexecution");
    public SoundEvent eexecution_se = SoundEvent.createVariableRangeEvent(eexecution_s);

    public final Identifier skeet_s = Identifier.parse("quantum:skeet");
    public SoundEvent skeet_se = SoundEvent.createVariableRangeEvent(skeet_s);

    public final Identifier neverlose_s = Identifier.parse("quantum:neverlose");
    public SoundEvent neverlose_se = SoundEvent.createVariableRangeEvent(neverlose_s);

    public void registerSounds() {

        Registry.register(BuiltInRegistries.SOUND_EVENT, dairpods_s, dairpods_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eairpods_s, eairpods_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dakrien_s, dakrien_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eakrien_s, eakrien_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dalarm_s, dalarm_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ealarm_s, ealarm_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, daugustus_s, daugustus_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eaugustus_s, eaugustus_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dbaloon_s, dbaloon_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ebaloon_s, ebaloon_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dbaloonfast_s, dbaloonfast_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ebaloonfast_s, ebaloonfast_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dbasic_s, dbasic_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ebasic_s, ebasic_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dbreak_s, dbreak_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ebreak_s, ebreak_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dbubble_s, dbubble_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ebubble_s, ebubble_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, ddiscord_s, ddiscord_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ediscord_s, ediscord_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dfrontiers_s, dfrontiers_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, efrontiers_s, efrontiers_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dheavy_s, dheavy_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eheavy_s, eheavy_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, disable_s, disable_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, enable_s, enable_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dnursultan_s, dnursultan_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, enursultan_s, enursultan_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dori_s, dori_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eori_s, eori_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dori2_s, dori2_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eori2_s, eori2_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dpiano_s, dpiano_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, epiano_s, epiano_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dsigma_s, dsigma_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, esigma_s, esigma_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dspeech_s, dspeech_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, espeech_s, espeech_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dspeechecho_s, dspeechecho_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, espeechecho_s, espeechecho_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dsweep_s, dsweep_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, esweep_s, esweep_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dtone_s, dtone_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, etone_s, etone_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dvega_s, dvega_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, evega_s, evega_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dvl_s, dvl_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, evl_s, evl_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dwood_s, dwood_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ewood_s, ewood_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dmouse_s, dmouse_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, emouse_s, emouse_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, dexecution_s, dexecution_se);
        Registry.register(BuiltInRegistries.SOUND_EVENT, eexecution_s, eexecution_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, skeet_s, skeet_se);

        Registry.register(BuiltInRegistries.SOUND_EVENT, neverlose_s, neverlose_se);
    }

    public void playSound(SoundEvent sound) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null)
            Minecraft.getInstance().level.playSound(Minecraft.getInstance().player,
                    Minecraft.getInstance().player.blockPosition(), sound, SoundSource.BLOCKS, 1f, 1f);
    }

    public void playEnable() {
        switch (ClientSounds.Mode.getCurrentMode().toLowerCase()) {
            case ("airpods"):
                playSound(eairpods_se);
                break;
            case ("akrien"):
                playSound(eakrien_se);
                break;
            case ("wood"):
                playSound(ewood_se);
                break;
            case ("alarm"):
                playSound(ealarm_se);
                break;
            case ("augustus"):
                playSound(eaugustus_se);
                break;
            case ("baloon"):
                playSound(ebaloon_se);
                break;
            case ("baloonfast"):
                playSound(ebaloonfast_se);
                break;
            case ("basic"):
                playSound(ebasic_se);
                break;
            case ("break"):
                playSound(ebreak_se);
                break;
            case ("bubble"):
                playSound(ebubble_se);
                break;
            case ("frontiers"):
                playSound(efrontiers_se);
                break;
            case ("vega"):
                playSound(evega_se);
                break;
            case ("vl"):
                playSound(evl_se);
                break;
            case ("nursultan"):
                playSound(enursultan_se);
                break;
            case ("ori2"):
                playSound(eori2_se);
                break;
            case ("ori"):
                playSound(eori_se);
                break;
            case ("discord"):
                playSound(ediscord_se);
                break;
            case ("piano"):
                playSound(epiano_se);
                break;
            case ("heavy"):
                playSound(eheavy_se);
                break;
            case ("sigma"):
                playSound(esigma_se);
                break;
            case ("speech"):
                playSound(espeech_se);
                break;
            case ("speechecho"):
                playSound(espeechecho_se);
                break;
            case ("sweep"):
                playSound(esweep_se);
                break;
            case ("tone"):
                playSound(etone_se);
                break;
            case ("mouse"):
                playSound(emouse_se);
                break;
            case ("execution"):
                playSound(eexecution_se);
                break;
        }
    }

    public void playDisable() {
        switch (ClientSounds.Mode.getCurrentMode().toLowerCase()) {
            case ("airpods"):
                playSound(dairpods_se);
                break;
            case ("akrien"):
                playSound(dakrien_se);
                break;
            case ("wood"):
                playSound(dwood_se);
                break;
            case ("alarm"):
                playSound(dalarm_se);
                break;
            case ("augustus"):
                playSound(daugustus_se);
                break;
            case ("baloon"):
                playSound(dbaloon_se);
                break;
            case ("baloonfast"):
                playSound(dbaloonfast_se);
                break;
            case ("basic"):
                playSound(dbasic_se);
                break;
            case ("break"):
                playSound(dbreak_se);
                break;
            case ("bubble"):
                playSound(dbubble_se);
                break;
            case ("frontiers"):
                playSound(dfrontiers_se);
                break;
            case ("vega"):
                playSound(dvega_se);
                break;
            case ("vl"):
                playSound(dvl_se);
                break;
            case ("nursultan"):
                playSound(dnursultan_se);
                break;
            case ("ori2"):
                playSound(dori2_se);
                break;
            case ("ori"):
                playSound(dori_se);
                break;
            case ("discord"):
                playSound(ddiscord_se);
                break;
            case ("piano"):
                playSound(dpiano_se);
                break;
            case ("heavy"):
                playSound(dheavy_se);
                break;
            case ("sigma"):
                playSound(dsigma_se);
                break;
            case ("speech"):
                playSound(dspeech_se);
                break;
            case ("speechecho"):
                playSound(dspeechecho_se);
                break;
            case ("sweep"):
                playSound(dsweep_se);
                break;
            case ("tone"):
                playSound(dtone_se);
                break;
            case ("mouse"):
                playSound(dmouse_se);
                break;
            case ("execution"):
                playSound(dexecution_se);
                break;
        }

    }

}
