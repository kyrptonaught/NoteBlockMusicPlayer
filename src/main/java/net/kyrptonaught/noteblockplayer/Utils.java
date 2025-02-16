package net.kyrptonaught.noteblockplayer;

import net.raphimc.noteblocklib.format.nbs.NbsDefinitions;
import net.raphimc.noteblocklib.format.nbs.NbsSong;
import net.raphimc.noteblocklib.util.MinecraftDefinitions;
import net.raphimc.noteblocklib.util.SongResampler;
import net.raphimc.noteblocklib.util.SongUtil;

public class Utils {

    public static void preProcessAllNotes(NbsSong song) {
        SongResampler.applyNbsTempoChangers(song);
        SongUtil.applyToAllNotes(song.getView(), note -> {

            String instrument;
            if (note.getCustomInstrument() == null) {
                instrument = note.getInstrument().mcSoundName();
            } else {
                instrument = note.getCustomInstrument().getName();
                note.setKey((byte) (note.getKey() + note.getCustomInstrument().getPitch() - 45));
            }

            int shift = MinecraftDefinitions.applyExtendedNotesResourcePack(note);
            instrument = instrument + (shift != 0 ? shift : "");
            instrument = instrument.toLowerCase().replaceAll("[^a-z0-9_.\\-:]", "");

            float pitch = MinecraftDefinitions.nbsPitchToMcPitch(NbsDefinitions.getPitch(note));
            note.setCustomInstrument(new PreParsedCustomInstrument(instrument, pitch));
        });

    }
}
