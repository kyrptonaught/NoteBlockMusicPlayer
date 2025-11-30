package net.kyrptonaught.noteblockplayer;

import net.raphimc.noteblocklib.data.MinecraftDefinitions;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.model.NbsCustomInstrument;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.SongResampler;

public class Utils {

    public static void preProcessAllNotes(Song song) {
        SongResampler.precomputeTempoEvents(song);
        song.getNotes().forEach(note -> {

            String instrument = "";
            if (note.getInstrument() instanceof MinecraftInstrument mc) {
                instrument = mc.mcSoundName();
            }
            if (note.getInstrument() instanceof NbsCustomInstrument custom) {
                instrument = custom.getName();
                note.setNbsKey((byte) (note.getNbsKey() + note.getPitch() - 45));
            }

            int shift = MinecraftDefinitions.applyExtendedNotesResourcePack(note);
            instrument = instrument + (shift != 0 ? "_" + shift : "");
            instrument = instrument.toLowerCase().replaceAll("[^a-z0-9_.\\-:]", "");

            note.setInstrument(new PreParsedCustomInstrument(instrument, note.getPitch()));
        });
    }
}
