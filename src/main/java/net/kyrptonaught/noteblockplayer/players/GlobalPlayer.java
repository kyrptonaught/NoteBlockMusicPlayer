package net.kyrptonaught.noteblockplayer.players;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.raphimc.noteblocklib.model.Song;

import java.util.Collection;

public class GlobalPlayer extends Player {

    public GlobalPlayer(Song song, String name, Collection<ServerPlayerEntity> players) {
        super(song, name, players);
    }

    @Override
    public void playNote(RegistryEntry<SoundEvent> soundEvent, float pitch, float volume) {
        for (ServerPlayerEntity player : players) {
            player.playSoundToPlayer(soundEvent.value(), SoundCategory.RECORDS, volume, pitch);
        }
    }
}
