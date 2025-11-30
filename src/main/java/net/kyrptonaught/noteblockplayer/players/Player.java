package net.kyrptonaught.noteblockplayer.players;

import net.kyrptonaught.noteblockplayer.Commands;
import net.kyrptonaught.noteblockplayer.PreParsedCustomInstrument;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.SongPlayer;

import java.util.Collection;
import java.util.List;

public class Player extends SongPlayer {

    protected final String name;
    protected final Collection<ServerPlayerEntity> players;
    protected boolean loop;
    protected int loopTick;

    public Player(Song song, String name, Collection<ServerPlayerEntity> players) {
        super(song);
        this.name = name;
        this.players = players;
    }

    public void setLoop(boolean loop, int loopTick) {
        this.loop = loop;
        this.loopTick = loopTick;
    }

    @Override
    protected void playNotes(List<Note> notes) {
        for (Note note : notes) playNote(note);
    }

    public void playNote(Note note) {
        if (note.getInstrument() instanceof PreParsedCustomInstrument nbsNote) {
            playNote(nbsNote.getInstrument(), nbsNote.getParsedPitch(), note.getVolume());
        }
    }

    public void playNote(RegistryEntry<SoundEvent> soundEvent, float pitch, float volume) {
    }

    @Override
    protected void onSongFinished() {
        super.onSongFinished();
        if (loop)
            start(0, loopTick);
        else
            Commands.stopSong(this.name);
    }
}
