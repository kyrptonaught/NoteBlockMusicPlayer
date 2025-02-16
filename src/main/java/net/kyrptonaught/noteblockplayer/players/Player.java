package net.kyrptonaught.noteblockplayer.players;

import net.kyrptonaught.noteblockplayer.Commands;
import net.kyrptonaught.noteblockplayer.PreParsedCustomInstrument;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.raphimc.noteblocklib.format.nbs.model.NbsNote;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.player.SongPlayerCallback;

import java.util.Collection;

public class Player implements SongPlayerCallback {

    protected final String name;
    protected final Collection<ServerPlayerEntity> players;
    protected boolean loop;
    protected int loopTick;

    public Player(String name, Collection<ServerPlayerEntity> players) {
        this.name = name;
        this.players = players;
    }

    public void setLoop(boolean loop, int loopTick) {
        this.loop = loop;
        this.loopTick = loopTick;
    }

    @Override
    public void playNote(Note note) {
        if (note instanceof NbsNote nbsNote) {
            final float volume = nbsNote.getVolume() / 100f;
            if (volume <= 0) return;

            if (nbsNote.getCustomInstrument() instanceof PreParsedCustomInstrument parsed) {
                playNote(parsed.getInstrument(), parsed.getParsedPitch(), volume);
            }
        }
    }

    public void playNote(RegistryEntry<SoundEvent> soundEvent, float pitch, float volume) {

    }

    @Override
    public boolean shouldLoop() {
        return loop;
    }

    @Override
    public int getLoopDelay() {
        return -loopTick;
    }

    @Override
    public void onFinished() {
        Commands.stopSong(this.name);
    }
}
